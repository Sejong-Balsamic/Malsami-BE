package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.DocumentCommand;
import com.balsamic.sejongmalsami.object.DocumentDto;
import com.balsamic.sejongmalsami.object.constants.UploadType;
import com.balsamic.sejongmalsami.object.postgres.DocumentFile;
import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.constants.DocumentType;
import com.balsamic.sejongmalsami.object.constants.PostTier;
import com.balsamic.sejongmalsami.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

  // 질문 게시글 등록
  @Transactional
  public DocumentDto saveDocumentPost(DocumentCommand command) {
    List<DocumentFile> documentFiles = new ArrayList<>(); // 저장된 자료 데이터

    // 회원 검증
    Member member = memberRepository.findById(command.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    log.info("회원 검증 완료: 회원ID={}", command.getMemberId());

    // 문서 파일 처리
    if (command.getDocumentFiles() != null && !command.getDocumentFiles().isEmpty()) {
      documentFiles.addAll(handleDocumentFiles(command));
    }

    // 이미지 파일 처리
    if (command.getImageFiles() != null && !command.getImageFiles().isEmpty()) {
      documentFiles.addAll(handleImageFiles(command));
    }

    // 미디어 파일 처리
    if (command.getMediaFiles() != null && !command.getMediaFiles().isEmpty()) {
      documentFiles.addAll(handleMediaFiles(command));
    }

    // 자료 게시글 저장
    DocumentPost documentPost = DocumentPost.builder()
        .member(member)
        .title(command.getTitle())
        .content(command.getContent())
        .subject(command.getSubject())
        .postTier(PostTier.CHEONMIN)
        .documentTypeSet(null)
        .likeCount(0)
        .commentCount(0)
        .viewCount(0)
        .isDepartmentPrivate(Boolean.TRUE.equals(command.getIsDepartmentPrivate()))
        .dailyScore(0)
        .weeklyScore(0)
        .build();

    // 자료 카테고리 설정 (최대 2개)
    if (command.getDocumentTypeSet() != null) {
      documentPost.updateDocumentTypeSet(new HashSet<>(command.getDocumentTypeSet()));
    }

    log.info("자료 게시글 객체 생성 완료: 제목={}", command.getTitle());

    DocumentPost savedDocument = documentPostRepository.save(documentPost);
    log.info("자료 게시글 저장 완료: 제목={} id={}", command.getTitle(), savedDocument.getDocumentPostId());

    return DocumentDto.builder()
        .documentPost(savedDocument)
        .documentFiles(documentFiles)
        .build();
  }

  private List<DocumentFile> handleDocumentFiles(DocumentCommand command) {
    log.info("문서 파일 처리 시작: 파일 개수={}", command.getDocumentFiles().size());
    List<DocumentFile> documentFiles = new ArrayList<>();
    for (MultipartFile file : command.getDocumentFiles()) {
      command.setUploadType(UploadType.DOCUMENT);
      command.setFile(file);
      DocumentFile documentFile = documentFileService.saveDocumentFile(command);
      documentFiles.add(documentFile);
      log.info("문서 파일 저장 완료: 업로드 파일명={}", documentFile.getUploadFileName());
    }
    return documentFiles;
  }

  private List<DocumentFile> handleImageFiles(DocumentCommand command) {
    log.info("이미지 파일 처리 시작: 파일 개수={}", command.getImageFiles().size());
    List<DocumentFile> imageFiles = new ArrayList<>();

    command.setUploadType(UploadType.IMAGE);

    // 단일 이미지
    if (command.getImageFiles().size() == 1) {
      command.setFile(command.getImageFiles().get(0));
      DocumentFile documentFile = documentFileService.saveDocumentFile(command);
      imageFiles.add(documentFile);
      log.info("단일 이미지 파일 저장 완료: 업로드 파일명={}", documentFile.getUploadFileName());
    } else {
      // 다중 이미지
      DocumentFile zipDocumentFile = documentFileService.saveImagesToDocumentFile(command);
      imageFiles.add(zipDocumentFile);
      log.info("다중 이미지 파일 ZIP 저장 완료: 업로드 파일명={}", zipDocumentFile.getUploadFileName());
    }
    return imageFiles;
  }

  private List<DocumentFile> handleMediaFiles(DocumentCommand command) {
    log.info("미디어 파일 처리 시작: 파일 개수={}", command.getMediaFiles().size());
    List<DocumentFile> mediaFiles = new ArrayList<>();
    for (MultipartFile file : command.getMediaFiles()) {
      command.setUploadType(UploadType.MEDIA);
      command.setFile(file);
      DocumentFile documentFile = documentFileService.saveMediaFile(command);
      mediaFiles.add(documentFile);
      log.info("미디어 파일 저장 완료: 업로드 파일명={}", documentFile.getUploadFileName());
    }
    return mediaFiles;
  }
}
