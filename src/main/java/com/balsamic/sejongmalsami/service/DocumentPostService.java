package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.DocumentCommand;
import com.balsamic.sejongmalsami.object.DocumentDto;
import com.balsamic.sejongmalsami.object.constants.DocumentType;
import com.balsamic.sejongmalsami.object.constants.MimeType;
import com.balsamic.sejongmalsami.object.constants.PostTier;
import com.balsamic.sejongmalsami.object.constants.SortType;
import com.balsamic.sejongmalsami.object.constants.UploadType;
import com.balsamic.sejongmalsami.object.postgres.DocumentFile;
import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
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

  private final DocumentPostRepository documentPostRepository;
  private final MemberRepository memberRepository;
  private final DocumentFileService documentFileService;

  /**
   * 자료 게시글 저장
   *
   * @param command 자료 게시글 정보
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

    // 첨부 자료 처리 및 저장 : 저장된 자료 파일은 savedDocumentFiles 에 추가
    processAndSaveUploadedFiles(command, savedDocumentFiles);

    // 자료 게시글 객체 생성 및 저장
    DocumentPost savedDocument = documentPostRepository.save(DocumentPost.builder()
        .member(member)
        .title(command.getTitle())
        .content(command.getContent())
        .subject(command.getSubject())
        .postTier(PostTier.CHEONMIN)
        .documentTypes(command.getDocumentTypes() != null ? new ArrayList<>(command.getDocumentTypes()) : null)
        .likeCount(0)
        .commentCount(0)
        .viewCount(0)
        .isDepartmentPrivate(Boolean.TRUE.equals(command.getIsDepartmentPrivate()))
        .dailyScore(0)
        .weeklyScore(0)
        .build());
    log.info("자료 게시글 저장 완료: 제목={} id={}", command.getTitle(), savedDocument.getDocumentPostId());

    return DocumentDto.builder()
        .documentPost(savedDocument)
        .documentFiles(savedDocumentFiles)
        .build();
  }

  //FIXME: 현재 틀만 만들어놓음 자료 LIST 제공 로직 필요
  @Transactional(readOnly = true)
  public DocumentDto searchDocumentPost(DocumentCommand command) {

    // 정렬
    Sort sort;
    String sortType = command.getSort();
    if ("likeCount".equalsIgnoreCase(sortType)) {
      sort = Sort.by(Order.desc("likeCount"));
    } else if ("viewCount".equalsIgnoreCase(sortType)) {
      sort = Sort.by(Order.desc("viewCount"));
    } else {
      sort = Sort.by(Order.desc("createdDate"));
    }

    Pageable pageable = PageRequest.of(command.getPageNumber(), command.getPageSize(), sort);

    //TODO: search 와 filter 분리 , like문 제거 및 필터링 변수 정의 추가 필요
    String title = command.getTitle();
    String subject = command.getSubject();
    String content = command.getContent();
    List<DocumentType> documentTypesList = command.getDocumentTypes();


    Page<DocumentPost> documentPostsPage = documentPostRepository.findDocumentPostsByFilter(
        title, subject, content, documentTypesList, pageable
    );

    return DocumentDto.builder()
        .documentPostsPage(documentPostsPage)
        .build();
  }

  @Transactional(readOnly = true)
  public DocumentDto getDocumentPosts(DocumentCommand command) {
    // 기본 : 천민 자료 반환
    //TODO: 게시글 등급 에 맞는 엽전 보유량 체크 필요

    // 정렬
    Sort sort = null;
    String sortType = command.getSort();
    if (sortType.equals(SortType.MOST_LIKED.name())) {
      sort = Sort.by(Order.desc("likeCount"));
    } else if (sortType.equals(SortType.VIEW_COUNT.name())) {
      sort = Sort.by(Order.desc("viewCount"));
    } else if (sortType.equals(null) || sortType.equals("") || sortType.equals(SortType.LATEST.name())) {
      sort = Sort.by(Order.desc("createdDate"));
    }

    Pageable pageable = PageRequest.of(command.getPageNumber(), command.getPageSize(), sort);

    //TODO: search 와 filter 분리 , like문 제거 및 필터링 변수 정의 추가 필요
    String title = command.getTitle();
    String subject = command.getSubject();
    String content = command.getContent();
    List<DocumentType> documentTypesList = command.getDocumentTypes();


    Page<DocumentPost> documentPostsPage = documentPostRepository.findDocumentPostsByFilter(
        title, subject, content, documentTypesList, pageable
    );

    return DocumentDto.builder()
        .documentPostsPage(documentPostsPage)
        .build();
  }

  /**
   * 첨부 파일 처리, 업로드, 저장
   *
   * <ul>
   *   <li>첨부 파일의 존재 여부를 확인</li>
   *   <li>각 파일의 MIME 타입에 따라 이미지, 동영상, 음악, 문서로 분류</li>
   *   <li>해당 파일 타입 매칭 -> 타입에 따른 썸네일 생성</li>
   *   <li>생성한 파일 및 썸네일 -> FTP 서버에 업로드</li>
   *   <li>파일의 메타데이터 {@link DocumentFile} -> DB에 저장</li>
   *   <li>저장된 {@link DocumentFile} 객체 -> savedDocumentFiles 추가</li>
   * </ul>
   *
   * @param command       DocumentCommand
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

        // UploadType에 따른 파일리스트에 파일 분류 작업
        if (uploadType == UploadType.DOCUMENT) {
          command.getDocumentFiles().add(file);
        } else if (uploadType == UploadType.IMAGE) {
          command.getImageFiles().add(file);
        } else if (uploadType == UploadType.VIDEO) {
          command.getVideoFiles().add(file);
        } else if (uploadType == UploadType.MUSIC) {
          command.getMusicFiles().add(file);
        } else {
          log.error("지원되지 않는 UploadType: {}", uploadType);
          throw new CustomException(ErrorCode.INVALID_UPLOAD_TYPE);
        }

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
}
