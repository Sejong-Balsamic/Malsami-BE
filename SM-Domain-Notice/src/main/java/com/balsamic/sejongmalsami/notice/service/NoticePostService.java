package com.balsamic.sejongmalsami.notice.service;

import static com.balsamic.sejongmalsami.constants.SortType.LATEST;
import static com.balsamic.sejongmalsami.constants.SortType.MOST_LIKED;
import static com.balsamic.sejongmalsami.constants.SortType.OLDEST;
import static com.balsamic.sejongmalsami.constants.SortType.VIEW_COUNT;
import static com.balsamic.sejongmalsami.constants.SortType.getJpqlSortOrder;

import com.balsamic.sejongmalsami.notice.dto.NoticePostCommand;
import com.balsamic.sejongmalsami.notice.dto.NoticePostDto;
import com.balsamic.sejongmalsami.notice.object.postgres.NoticePost;
import com.balsamic.sejongmalsami.notice.repository.postgres.NoticePostRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.List;
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

  private final NoticePostRepository noticePostRepository;

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
}
