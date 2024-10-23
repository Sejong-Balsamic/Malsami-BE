package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.DocumentCommand;
import com.balsamic.sejongmalsami.object.DocumentDto;
import com.balsamic.sejongmalsami.object.constants.MimeType;
import com.balsamic.sejongmalsami.object.constants.UploadType;
import com.balsamic.sejongmalsami.object.postgres.DocumentFile;
import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.constants.PostTier;
import com.balsamic.sejongmalsami.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentPostService {

  private final DocumentPostRepository documentPostRepository;
  private final MemberRepository memberRepository;
  private final DocumentFileService documentFileService;

  /**
   * 자료 게시글 저장 메서드
   */
  @Transactional
  public DocumentDto saveDocumentPost(DocumentCommand command) {
    List<DocumentFile> documentFiles = new ArrayList<>(); // 저장된 자료 파일들

    // 회원 검증
    Member member = memberRepository.findById(command.getMemberId())
        .orElseThrow(() -> {
          log.error("회원이 존재하지 않습니다: memberId={}", command.getMemberId());
          return new CustomException(ErrorCode.MEMBER_NOT_FOUND);
        });
    log.info("회원 검증 완료: 회원ID={}", command.getMemberId());

    // 업로드 타입에 따른 파일 처리
    processUploadTypes(command, documentFiles);

    // 자료 게시글 객체 생성
    DocumentPost documentPost = createDocumentPost(command, member);
    log.info("자료 게시글 객체 생성 완료: 제목={}", command.getTitle());

    // 자료 게시글 저장
    DocumentPost savedDocument = documentPostRepository.save(documentPost);
    log.info("자료 게시글 저장 완료: 제목={} id={}", command.getTitle(), savedDocument.getDocumentPostId());

    return DocumentDto.builder()
        .documentPost(savedDocument)
        .documentFiles(documentFiles)
        .build();
  }

  /**
   * 업로드 타입에 따른 파일 처리 메서드
   */
  private void processUploadTypes(DocumentCommand command, List<DocumentFile> documentFiles) {
    List<MultipartFile> attachmentFiles = command.getAttachmentFiles();

    // 첨부파일이 없을 때
    if (attachmentFiles == null || attachmentFiles.isEmpty()) {
      log.warn("첨부된 파일이 없습니다.");
      return;
    }

    for (MultipartFile file : attachmentFiles) {
      try {
        String mimeType = file.getContentType();
        if (mimeType == null) {
          log.error("파일의 MIME 타입을 확인할 수 없습니다: {}", file.getOriginalFilename());
          throw new CustomException(ErrorCode.INVALID_FILE_FORMAT);
        }

        // UploadType 결정
        MimeType type = MimeType.fromString(mimeType);
        UploadType uploadType = type.getUploadType();

        // 해당 UploadType에 따라 파일 리스트에 추가
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
        documentFiles.add(savedFile);

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

  /**
   * 자료 게시글 객체를 생성합니다.
   */
  private DocumentPost createDocumentPost(DocumentCommand command, Member member) {
    return DocumentPost.builder()
        .member(member)
        .title(command.getTitle())
        .content(command.getContent())
        .subject(command.getSubject())
        .postTier(PostTier.CHEONMIN)
        .documentTypeSet(command.getDocumentTypeSet() != null
            ? new HashSet<>(command.getDocumentTypeSet())
            : null)
        .likeCount(0)
        .commentCount(0)
        .viewCount(0)
        .isDepartmentPrivate(Boolean.TRUE.equals(command.getIsDepartmentPrivate()))
        .dailyScore(0)
        .weeklyScore(0)
        .build();
  }
}
