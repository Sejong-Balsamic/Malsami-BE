package com.balsamic.sejongmalsami.service;

import static com.balsamic.sejongmalsami.util.LogUtils.lineLog;
import static com.balsamic.sejongmalsami.util.LogUtils.superLog;

import com.balsamic.sejongmalsami.object.DocumentCommand;
import com.balsamic.sejongmalsami.object.DocumentDto;
import com.balsamic.sejongmalsami.object.constants.DocumentType;
import com.balsamic.sejongmalsami.object.constants.Faculty;
import com.balsamic.sejongmalsami.object.constants.SortType;
import com.balsamic.sejongmalsami.object.postgres.Course;
import com.balsamic.sejongmalsami.object.postgres.DocumentRequestPost;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
import com.balsamic.sejongmalsami.repository.postgres.CourseRepository;
import com.balsamic.sejongmalsami.repository.postgres.DocumentRequestPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentRequestPostService {

  private static final Integer DOCUMENT_TYPE_LIMIT = 3;

  private final DocumentRequestPostRepository documentRequestPostRepository;
  private final MemberRepository memberRepository;
  private final YeopjeonService yeopjeonService;
  private final CourseRepository courseRepository;

  /**
   * <h3>자료요청 글 작성</h3>
   * <p>자료요청 게시판은 '중인(엽전 수 : 1000개)' 이상 접근 가능합니다.</p>
   * <p>학과는 기본적으로 로그인한 사용자의 학과를 선택합니다.</p>
   *
   * @param command memberId, title, content, faculty, documentTypes, isPrivate
   * @return
   */
  public DocumentDto createPost(DocumentCommand command) {

    // 사용자 확인
    Member member = memberRepository.findById(command.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    // 사용자가 중인 이상인지 검증
    validateJunginOrAbove(member);

    // 입력한 교과목명에 따른 단과대 설정 (교과목명 존재할 경우)
    List<Faculty> faculties = null;
    if (command.getSubject() != null) {
      faculties = courseRepository
          .findAllBySubject(command.getSubject())
          .stream().map(Course::getFaculty).collect(Collectors.toList());
      log.info("입력된 교과목명 : {}", command.getSubject());
      log.info("단과대 List : {}", faculties);

      if (faculties.isEmpty()) {
        throw new CustomException(ErrorCode.FACULTY_NOT_FOUND);
      }
    }

    // 자료 타입 추가 (선택)
    List<DocumentType> documentTypes = null;
    if (!command.getDocumentTypes().isEmpty()) {
      if (command.getDocumentTypes().size() < DOCUMENT_TYPE_LIMIT) {
        documentTypes = command.getDocumentTypes();
      } else {
        throw new CustomException(ErrorCode.DOCUMENT_TYPE_LIMIT_EXCEEDED);
      }
    }

    DocumentRequestPost documentRequestPost = DocumentRequestPost.builder()
        .member(member)
        .title(command.getTitle())
        .content(command.getContent())
        .subject(command.getSubject())
        .faculties(faculties)
        .documentTypes(documentTypes)
        .viewCount(0)
        .likeCount(0)
        .commentCount(0)
        .isPrivate(Boolean.TRUE.equals(command.getIsPrivate()))
        .build();

    lineLog(null);
    superLog(documentRequestPost);
    lineLog(null);

    return DocumentDto.builder()
        .documentRequestPost(documentRequestPostRepository.save(documentRequestPost))
        .build();
  }

  /**
   * <h3>자료요청글 필터링 로직</h3>
   * <p>자료요청 게시판은 '중인(엽전 수 : 1000개)' 이상 접근 가능합니다.</p>
   * <p>1. 교과목명 기준 검색 - String subject (ex.컴퓨터구조, 인터렉티브 디자인)
   * <p>2. 학부 기준 검색 - Faculty (ex.대양휴머니티칼리지)</p>
   * <p>3. 카테고리 검색 - DocumentType (ex.DocumentType.SOLUTION)</p>
   * <br>
   * <h3>정렬 타입 (SortType)</h3>
   * <p>최신순, 좋아요순, 댓글순, 조회순</p>
   *
   * @param command memberId, subject, faculty, documentType, sortType
   * @return
   */
  public DocumentDto filteredDocumentRequests(DocumentCommand command) {

    Member member = memberRepository.findById(command.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    // 해당 사용자가 중인 이상인지 검증
    validateJunginOrAbove(member);

    // 과목명이 비어있는 경우 null 설정 (비어있는 경우 쿼리문에서 오류 발생)
    if (command.getSubject() != null && command.getSubject().isEmpty()) {
      command.setSubject(null);
    }

    // 카테고리 List 사이즈가 0인 경우 null로 설정 (비어있는 list의 경우 쿼리문에서 오류 발생)
    if (command.getDocumentTypes() != null && command.getDocumentTypes().isEmpty()) {
      command.setDocumentTypes(null);
    }

    // 정렬 기준 (default: 최신순)
    SortType sortType = (command.getSortType() != null) ? command.getSortType() : SortType.LATEST;

    Sort sort;
    switch (sortType) {
      case LATEST -> sort = Sort.by(Direction.DESC, "createdDate");
      case MOST_LIKED -> sort = Sort.by(Direction.DESC, "likeCount");
      case COMMENT_COUNT -> sort = Sort.by(Direction.DESC, "commentCount");
      case VIEW_COUNT -> sort = Sort.by(Direction.DESC, "viewCount");
      default -> sort = Sort.by(Direction.DESC, "createdDate");
    }

    Pageable pageable = PageRequest.of(command.getPageNumber(), command.getPageSize(), sort);

    Page<DocumentRequestPost> posts = documentRequestPostRepository.findFilteredDocumentRequestPost(
        command.getSubject(),
        command.getFaculty(),
        command.getDocumentTypes(),
        pageable
    );

    return DocumentDto.builder()
        .documentRequestPostsPage(posts)
        .build();
  }

  /**
   * <h3>특정 자료 요청 글 조회</h3>
   * <p>자료요청 게시판은 '중인(엽전 수: 1000개)' 이상 접근 가능합니다.
   * <ul>
   *   <li>해당 글 조회 수 증가</li>
   * </ul>
   *
   * @param command memberId, documentPostId
   * @return
   */
  public DocumentDto getDocumentRequestPost(DocumentCommand command) {

    Member member = memberRepository.findById(command.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    // 해당 사용자가 중인 이상인지 검증
    validateJunginOrAbove(member);

    DocumentRequestPost post = documentRequestPostRepository
        .findById(command.getDocumentPostId())
        .orElseThrow(() -> new CustomException(ErrorCode.DOCUMENT_REQUEST_POST_NOT_FOUND));

    lineLog(null);
    superLog(post);
    lineLog(null);

    // 해당 글 조회 수 증가
    post.increaseViewCount();

    return DocumentDto.builder()
        .documentRequestPost(post)
        .build();
  }

  // 해당 member가 중인 이상인지 검증하는 메소드
  private void validateJunginOrAbove(Member member) {
    // '중인' (엽전수 1000개)이상 접근 가능
    Yeopjeon yeopjeon = yeopjeonService.findMemberYeopjeon(member);
    if (yeopjeon.getYeopjeon() < 1000) {
      log.error("자료요청게시판은 중인 이상 접근이 가능합니다. {} 의 엽전 수: {}",
          member.getStudentId(),
          yeopjeon.getYeopjeon());
      throw new CustomException(ErrorCode.INSUFFICIENT_YEOPJEON);
    }
  }
}
