package com.balsamic.sejongmalsami.service;

import static com.balsamic.sejongmalsami.object.constants.YeopjeonAction.VIEW_DOCUMENT_CHEONMIN_POST;
import static com.balsamic.sejongmalsami.object.constants.YeopjeonAction.VIEW_DOCUMENT_JUNGIN_POST;
import static com.balsamic.sejongmalsami.object.constants.YeopjeonAction.VIEW_DOCUMENT_KING_POST;
import static com.balsamic.sejongmalsami.object.constants.YeopjeonAction.VIEW_DOCUMENT_YANGBAN_POST;

import com.balsamic.sejongmalsami.object.DocumentCommand;
import com.balsamic.sejongmalsami.object.DocumentDto;
import com.balsamic.sejongmalsami.object.constants.Faculty;
import com.balsamic.sejongmalsami.object.constants.MimeType;
import com.balsamic.sejongmalsami.object.constants.PostTier;
import com.balsamic.sejongmalsami.object.constants.SortType;
import com.balsamic.sejongmalsami.object.constants.UploadType;
import com.balsamic.sejongmalsami.object.postgres.Course;
import com.balsamic.sejongmalsami.object.postgres.DocumentFile;
import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
import com.balsamic.sejongmalsami.repository.mongo.DocumentBoardLikeRepository;
import com.balsamic.sejongmalsami.repository.postgres.CourseRepository;
import com.balsamic.sejongmalsami.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.util.config.YeopjeonConfig;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentPostService {

  private final static int MAX_DOCUMENT_TYPES = 2; // 태그 최대 개수 제한

  private final DocumentPostRepository documentPostRepository;
  private final MemberRepository memberRepository;
  private final DocumentFileService documentFileService;
  private final DocumentBoardLikeRepository documentBoardLikeRepository;
  private final CourseRepository courseRepository;
  private final YeopjeonService yeopjeonService;
  private final YeopjeonConfig yeopjeonConfig;

  /**
   * <h3>자료 글 저장
   *
   * @param command memberId, title, content, subject, documentTypes, isDepartmentPrivate
   * @return 저장된 DocumentDto
   */
  @Transactional
  public DocumentDto saveDocumentPost(DocumentCommand command) {
    List<DocumentFile> savedDocumentFiles = new ArrayList<>(); // 저장된 자료 파일들

    // 회원 검증
    Member member = memberRepository.findById(command.getMemberId())
        .orElseThrow(() -> {
          log.error("회원이 존재하지 않습니다: memberId={}", command.getMemberId());
          return new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        });
    log.info("회원 검증 완료: studentId={}", member.getStudentId());


    // 입력된 교과목에 따른 단과대 설정
    List<Faculty> faculties = courseRepository
        .findAllBySubject(command.getSubject())
        .stream().map(Course::getFaculty)
        .collect(Collectors.toList());

    log.info("입력된 교과목명 : {}", command.getSubject());
    log.info("단과대 List : {}", faculties);

    if (faculties.isEmpty()) {
      throw new CustomException(ErrorCode.FACULTY_NOT_FOUND);
    }

    // 자료 게시글 객체 생성 및 저장
    DocumentPost savedDocument = documentPostRepository.save(DocumentPost.builder()
        .member(member)
        .title(command.getTitle())
        .content(command.getContent())
        .subject(command.getSubject())
        .faculties(faculties)
        .postTier(PostTier.CHEONMIN)
        .documentTypes(command.getDocumentTypes() != null ? new ArrayList<>(command.getDocumentTypes()) : null)
        .likeCount(0)
        .commentCount(0)
        .viewCount(0)
        .isDepartmentPrivate(Boolean.TRUE.equals(command.getIsDepartmentPrivate()))
        .dailyScore(0L)
        .weeklyScore(0L)
        .build());
    log.info("자료 게시글 저장 완료: 제목={} id={}", command.getTitle(), savedDocument.getDocumentPostId());

    // 첨부 자료 처리 및 저장 : 저장된 자료 파일은 savedDocumentFiles 에 추가
    command.setDocumentPostId(savedDocument.getDocumentPostId()); // 생성된 DocumentPostId 전달
    processAndSaveUploadedFiles(command, savedDocumentFiles);

    return DocumentDto.builder()
        .documentPost(savedDocument)
        .documentFiles(savedDocumentFiles)
        .build();
  }

  /**
   * <h3>자료 글 필터링 조회</h3>
   * <ul>
   *   <li>과목 필터링</li>
   *   <li>태그 필터링</li>
   *   <li>단과대 필터링</li>
   *   <li>자료등급 필터링</li>
   * </ul>
   * <p>정렬 타입</p>
   * 최신순, 좋아요순, 조회순
   *
   * @param command memberId, subject, documentTypes, faculty, postTier, sortType, pageNumber, pageSize
   * @return
   */
  @Transactional(readOnly = true)
  public DocumentDto filteredDocumentPost(DocumentCommand command) {

    Member member = memberRepository.findById(command.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    PostTier postTier = command.getPostTier();

    // 현재 사용자의 해당 게시판 접근 가능 여부 확인
    canAccessDocumentBoard(member, postTier);

    // 태그 필터링 최대 2개까지 선택가능
    if (command.getDocumentTypes().size() > MAX_DOCUMENT_TYPES) {
      throw new CustomException(ErrorCode.DOCUMENT_TYPE_LIMIT_EXCEEDED);
    }

    // 정렬 (최신순, 좋아요순, 조회순)
    Sort sort;
    SortType sortType = command.getSortType();
    if (sortType.equals(SortType.MOST_LIKED)) {
      sort = Sort.by(Order.desc("likeCount"));
    } else if (sortType.equals(SortType.VIEW_COUNT)) {
      sort = Sort.by(Order.desc("viewCount"));
    } else {
      sort = Sort.by(Order.desc("createdDate"));
    }

    Pageable pageable = PageRequest.of(
        command.getPageNumber(),
        command.getPageSize(),
        sort
    );

    Page<DocumentPost> documentPostsPage = documentPostRepository.findDocumentPostsByFilter(
        command.getSubject(),
        command.getDocumentTypes(),
        command.getFaculty(),
        postTier,
        pageable
    );

    return DocumentDto.builder()
        .documentPostsPage(documentPostsPage)
        .build();
  }

  /**
   * <h3>특정 자료 글 조회</h3>
   * <ul>
   *   <li>해당 글 조회수 증가</li>
   *   <li>게시판 등급에 따라 사용자 엽전 감소</li>
   *   <li>사용자가 좋아요 누른 글 여부 반환</li>
   * </ul>
   *
   * @param command memberId, documentPostId
   * @return
   */
  @Transactional
  public DocumentDto getDocumentPost(DocumentCommand command) {

    Member member = memberRepository.findById(command.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    DocumentPost post = documentPostRepository.findById(command.getDocumentPostId())
        .orElseThrow(() -> new CustomException(ErrorCode.DOCUMENT_POST_NOT_FOUND));

    PostTier postTier = post.getPostTier();

    // 해당 게시판 접근 가능 여부 확인
    canAccessDocumentBoard(member, postTier);

    // 게시글 등급에 따라 사용자 엽전 변동 및 엽전 히스토리 저장
    switch (postTier) {
      case CHEONMIN -> yeopjeonService
          .updateYeopjeonAndSaveYeopjeonHistory(member, VIEW_DOCUMENT_CHEONMIN_POST);
      case JUNGIN -> yeopjeonService
          .updateYeopjeonAndSaveYeopjeonHistory(member, VIEW_DOCUMENT_JUNGIN_POST);
      case YANGBAN -> yeopjeonService
          .updateYeopjeonAndSaveYeopjeonHistory(member, VIEW_DOCUMENT_YANGBAN_POST);
      case KING -> yeopjeonService
          .updateYeopjeonAndSaveYeopjeonHistory(member, VIEW_DOCUMENT_KING_POST);
    }

    // 해당 자료 글 조회수 증가
    post.increaseViewCount();

    // 사용자가 좋아요를 눌렀는지 확인
    Boolean isLiked = documentBoardLikeRepository
        .existsByDocumentBoardIdAndMemberId(post.getDocumentPostId(), command.getMemberId());
    post.updateIsLiked(isLiked);

    // 해당 자료 글 반환
    return DocumentDto.builder()
        .documentPost(documentPostRepository.save(post))
        .build();
  }

  /**
   * 첨부 파일 처리, 업로드, 저장
   *
   * @param command            DocumentCommand
   * @param savedDocumentFiles 저장된 파일 리스트
   */
  private void processAndSaveUploadedFiles(DocumentCommand command, List<DocumentFile> savedDocumentFiles) {
    List<MultipartFile> attachmentFiles = command.getAttachmentFiles();

    // 첨부파일 리스트에 첨부된 파일이 없을 때
    if (attachmentFiles == null || attachmentFiles.isEmpty()) {
      log.info("첨부된 파일이 없습니다.");
      return;
    }

    // 첨부파일 리스트에서 파일 순회
    for (MultipartFile file : attachmentFiles) {
      try {
        String mimeType = file.getContentType();
        if (mimeType == null) {
          log.error("파일의 MIME 타입을 확인할 수 없습니다: {}", file.getOriginalFilename());
          throw new CustomException(ErrorCode.INVALID_FILE_FORMAT);
        }

        // UploadType 결정
        UploadType uploadType = MimeType.fromString(mimeType).getUploadType();

        // 파일 유효성 검사
        documentFileService.validateFile(file, uploadType);

        // 파일 저장
        DocumentFile savedFile = documentFileService.saveFile(command, uploadType, file);
        savedDocumentFiles.add(savedFile);

        log.info("파일 저장 완료: 업로드 파일명={}", savedFile.getUploadFileName());

      } catch (CustomException e) {
        log.error("파일 처리 중 오류 발생: {}", e.getMessage());
        throw e; // 트랜잭션 롤백을 위해 예외 다시 던지기
      } catch (Exception e) {
        log.error("파일 처리 중 예상치 못한 오류 발생: {}", e.getMessage());
        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
      }
    }
  }

  // 해당 자료 게시판 접근 가능 여부 판단 메소드
  private void canAccessDocumentBoard(Member member, PostTier postTier) {
    Yeopjeon yeopjeon = yeopjeonService.findMemberYeopjeon(member);

    // 게시판 접근 가능 여부 확인
    if (postTier.equals(PostTier.CHEONMIN)) { // 천민 게시판 접근 시
      log.info("천민 게시판 접근, 현재 사용자 {}의 엽전개수: {}", member.getStudentId(), yeopjeon.getYeopjeon());
    } else if (postTier.equals(PostTier.JUNGIN)) { // 중인 게시판 접근 시
      if (yeopjeon.getYeopjeon() < yeopjeonConfig.getJunginRequirement()) {
        log.error("현재 사용자 {}의 엽전이 부족하여 중인게시판에 접근할 수 없습니다.", member.getStudentId());
        log.error("중인 게시판 엽전 기준: {}냥, 현재 사용자 엽전개수: {}", yeopjeonConfig.getJunginRequirement(), yeopjeon.getYeopjeon());
        throw new CustomException(ErrorCode.INSUFFICIENT_YEOPJEON);
      }
    } else if (postTier.equals(PostTier.YANGBAN)) { // 양반 게시판 접근 시
      if (yeopjeon.getYeopjeon() < yeopjeonConfig.getYangbanRequirement()) {
        log.error("현재 사용자 {}의 엽전이 부족하여 양반게시판에 접근할 수 없습니다.", member.getStudentId());
        log.error("양반 게시판 엽전 기준: {}냥, 현재 사용자 엽전개수: {}", yeopjeonConfig.getYangbanRequirement(), yeopjeon.getYeopjeon());
        throw new CustomException(ErrorCode.INSUFFICIENT_YEOPJEON);
      }
    } else if (postTier.equals(PostTier.KING)) { // 왕 게시판 접근 시
      if (yeopjeon.getYeopjeon() < yeopjeonConfig.getKingRequirement()) {
        log.error("현재 사용자 {}의 엽전이 부족하여 왕 게시판에 접근할 수 없습니다.", member.getStudentId());
        log.error("왕 게시판 엽전 기준: {}냥, 현재 사용자 엽전개수: {}", yeopjeonConfig.getKingRequirement(), yeopjeon.getYeopjeon());
        throw new CustomException(ErrorCode.INSUFFICIENT_YEOPJEON);
      }
    } else {
      throw new CustomException(ErrorCode.INVALID_POST_TIER);
    }
  }
}
