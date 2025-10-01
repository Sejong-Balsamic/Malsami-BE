package com.balsamic.sejongmalsami.notice.service;

import static com.balsamic.sejongmalsami.constants.ContentType.NOTICE;
import static com.balsamic.sejongmalsami.constants.SortType.LATEST;
import static com.balsamic.sejongmalsami.constants.SortType.MOST_LIKED;
import static com.balsamic.sejongmalsami.constants.SortType.OLDEST;
import static com.balsamic.sejongmalsami.constants.SortType.VIEW_COUNT;
import static com.balsamic.sejongmalsami.constants.SortType.getJpqlSortOrder;

import com.balsamic.sejongmalsami.notice.dto.NoticePostCommand;
import com.balsamic.sejongmalsami.notice.dto.NoticePostDto;
import com.balsamic.sejongmalsami.notice.object.mongo.NoticeBoardLike;
import com.balsamic.sejongmalsami.notice.object.postgres.NoticePost;
import com.balsamic.sejongmalsami.notice.repository.mongo.NoticeBoardLikeRepository;
import com.balsamic.sejongmalsami.notice.repository.postgres.NoticePostRepository;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.util.RedisLockManager;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class NoticePostService {

  private static final long WAIT_TIME = 5L; // Lock을 얻기위해 기다리는 시간
  private static final long LEASE_TIME = 2L; // Lock 자동 해제 시간

  private final NoticePostRepository noticePostRepository;
  private final NoticeBoardLikeRepository noticeBoardLikeRepository;
  private final MemberRepository memberRepository;
  private final RedisLockManager redisLockManager;

  /**
   * 공지사항 필터링 글 조회 필터링 조건에 맞는 전체 글을 반환합니다
   * [필터링] 1. 제목 필터링 (검색어 포함)
   * [sortType] 1. 최신순 2. 과거순 3. 조회순 4. 추천순
   *
   * @param command query, sortType, sortField, sortDirection, pageNumber, pageSize
   * @return
   */
  public NoticePostDto getFilteredPost(NoticePostCommand command) {

    // query가 비어있는 경우 null 설정
    if (command.getQuery() != null && command.getQuery().isBlank()) {
      command.setQuery(null);
    }

    Sort sort;
    if (command.getSortType() != null) { // sortType 이 요청된 경우
      // 정렬 조건
      if (!command.getSortType().equals(LATEST) &&
          !command.getSortType().equals(OLDEST) &&
          !command.getSortType().equals(VIEW_COUNT) &&
          !command.getSortType().equals(MOST_LIKED)) {
        log.error("잘못된 정렬 조건입니다. 요청 SortType: {}", command.getSortType());
        throw new CustomException(ErrorCode.INVALID_SORT_TYPE);
      }
      sort = getJpqlSortOrder(command.getSortType());
    } else { // sortField, sortDirection 이 요청된 경우
      String sortField = (command.getSortField() != null) ? command.getSortField() : "createdDate";
      String sortDirStr = (command.getSortDirection() != null) ? command.getSortDirection().toUpperCase() : "DESC";

      // Sort Direction 파싱
      Sort.Direction direction;
      try {
        direction = Sort.Direction.valueOf(sortDirStr); // "ASC" or "DESC"
      } catch (Exception e) {
        direction = Sort.Direction.DESC; // fallback
      }

      // Sort 객체
      sort = Sort.by(direction, sortField);
    }

    // Pageable
    Pageable pageable = PageRequest.of(
        command.getPageNumber(),
        command.getPageSize(),
        sort
    );

    Page<NoticePost> noticePostPage = noticePostRepository
        .findNoticePostsByFilter(command.getQuery(), pageable);

    return NoticePostDto.builder()
        .noticePostsPage(noticePostPage)
        .build();
  }

  /**
   * PIN 된 공지사항 List를 반환합니다
   */
  @Transactional(readOnly = true)
  public NoticePostDto getPinnedPost() {
    List<NoticePost> noticePosts = noticePostRepository.findAllByIsPinned(true);
    return NoticePostDto.builder()
        .noticePosts(noticePosts)
        .build();
  }

  /**
   * 단일 공지사항 글 조회
   * 조회 시 조회수를 증가시킵니다
   *
   * @param command noticePostId
   * @return NoticePostDto
   */
  @Transactional
  public NoticePostDto getNoticePost(NoticePostCommand command) {
    if (command.getNoticePostId() == null) {
      log.error("공지사항 ID가 없습니다.");
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }

    NoticePost noticePost = noticePostRepository.findById(command.getNoticePostId())
        .orElseThrow(() -> {
          log.error("공지사항을 찾을 수 없습니다. ID: {}", command.getNoticePostId());
          return new CustomException(ErrorCode.NOTICE_POST_NOT_FOUND);
        });

    // 조회수 증가
    noticePost.increaseViewCount();
    noticePostRepository.save(noticePost);

    return NoticePostDto.builder()
        .noticePost(noticePost)
        .build();
  }

  /**
   * <h3>공지사항 좋아요 로직</h3>
   * <ul>
   *   <li>본인이 작성한 글에 좋아요 불가</li>
   *   <li>이미 좋아요 누른 글에 중복 요청 불가</li>
   * </ul>
   *
   * @param command memberId, postId, contentType
   * @return 공지사항 좋아요 내역
   */
  @Transactional
  public NoticePostDto noticePostLike(NoticePostCommand command) {
    UUID memberId = command.getMemberId();
    UUID postId = command.getPostId();

    // 락 획득 시도 (락 키는 게시글 PK)
    String lockKey = "lock:like:" + postId;

    return redisLockManager.executeLock(lockKey, WAIT_TIME, LEASE_TIME, () -> {

      // 회원 조회
      Member curMember = memberRepository.findById(memberId)
          .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

      // 공지사항 글 조회
      NoticePost noticePost = noticePostRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_POST_NOT_FOUND));

      Member writer = noticePost.getMember();

      // 자기 자신에게 요청여부 검증
      validateSelfAction(curMember, writer);

      // 이미 좋아요를 누른 글인지 검증
      checkAlreadyAction(memberId, postId);

      try {
        // 좋아요 증가
        noticePost.increaseLikeCount();
        noticePostRepository.save(noticePost);

        // 좋아요 히스토리 저장
        NoticeBoardLike noticeBoardLike = NoticeBoardLike.builder()
            .memberId(memberId)
            .noticePostId(postId)
            .contentType(NOTICE)
            .build();
        noticeBoardLikeRepository.save(noticeBoardLike);

        return NoticePostDto.builder()
            .noticeBoardLike(noticeBoardLike)
            .build();
      } catch (Exception e) {
        log.error("좋아요 변동 중 오류가 발생했습니다.", e);
        // 좋아요 수 롤백
        rollbackLikeCount(postId);
        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
      }
    });
  }

  /**
   * <h3>공지사항 좋아요 취소 로직</h3>
   * <p>이미 좋아요를 누른 공지사항에 대해서만 취소 가능</p>
   *
   * @param command memberId, postId, contentType
   */
  @Transactional
  public void cancelNoticePostLike(NoticePostCommand command) {
    UUID memberId = command.getMemberId();
    UUID postId = command.getPostId();

    // 락 획득 시도 (락 키는 게시글 PK)
    String lockKey = "lock:like:" + postId;

    redisLockManager.executeLock(lockKey, WAIT_TIME, LEASE_TIME, () -> {

      // 회원 조회
      Member curMember = memberRepository.findById(memberId)
          .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

      // 공지사항 글 조회
      NoticePost noticePost = noticePostRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_POST_NOT_FOUND));

      // 좋아요 히스토리가 존재하는지 검증
      checkCancelLikeHistoryExists(memberId, postId);

      try {
        // 좋아요 개수 감소
        noticePost.decreaseLikeCount();
        noticePostRepository.save(noticePost);

        // 좋아요 히스토리 삭제
        noticeBoardLikeRepository.deleteByNoticePostIdAndMemberId(postId, memberId);

        return null;
      } catch (Exception e) {
        log.error("좋아요 취소 중 오류가 발생했습니다.", e);
        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
      }
    });
  }

  // 자기 자신에게 요청했는지 검증
  private void validateSelfAction(Member curMember, Member writer) {
    if (curMember.equals(writer)) {
      log.error("본인 글에 좋아요를 누를 수 없습니다. 로그인 사용자: {}, 글 작성자: {}",
          curMember.getStudentId(), writer.getStudentId());
      throw new CustomException(ErrorCode.SELF_ACTION_NOT_ALLOWED);
    }
  }

  /**
   * <h3>이미 좋아요를 눌렀는지 검증</h3>
   *
   * @param memberId 로그인 회원
   * @param postId   게시물
   */
  private void checkAlreadyAction(UUID memberId, UUID postId) {
    boolean exists = noticeBoardLikeRepository.existsByNoticePostIdAndMemberId(postId, memberId);

    if (exists) {
      Member curMember = memberRepository.findById(memberId)
          .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
      log.error("이미 좋아요를 누른 글입니다. postId: {}, 로그인한 사용자: {}",
          postId, curMember.getStudentId());
      throw new CustomException(ErrorCode.ALREADY_ACTION);
    }
  }

  /**
   * <h3>좋아요 히스토리 존재 여부 검증 (취소용)</h3>
   *
   * @param memberId 로그인 회원
   * @param postId   게시물
   */
  private void checkCancelLikeHistoryExists(UUID memberId, UUID postId) {
    boolean exists = noticeBoardLikeRepository.existsByNoticePostIdAndMemberId(postId, memberId);

    if (!exists) {
      Member curMember = memberRepository.findById(memberId)
          .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
      log.error("취소할 좋아요가 없습니다. postId: {}, 로그인한 사용자: {}",
          postId, curMember.getStudentId());
      throw new CustomException(ErrorCode.LIKE_HISTORY_NOT_FOUND);
    }
  }

  /**
   * <h3>좋아요 수 롤백</h3>
   *
   * @param postId 게시물
   */
  private void rollbackLikeCount(UUID postId) {
    NoticePost noticePost = noticePostRepository.findById(postId)
        .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_POST_NOT_FOUND));
    noticePost.decreaseLikeCount();
    noticePostRepository.save(noticePost);
  }
}
