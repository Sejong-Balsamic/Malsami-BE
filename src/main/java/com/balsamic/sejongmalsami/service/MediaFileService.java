package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.constants.MimeType;
import com.balsamic.sejongmalsami.object.postgres.AnswerPost;
import com.balsamic.sejongmalsami.object.postgres.Comment;
import com.balsamic.sejongmalsami.object.postgres.DocumentRequestPost;
import com.balsamic.sejongmalsami.object.postgres.MediaFile;
import com.balsamic.sejongmalsami.object.postgres.NoticePost;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.repository.postgres.AnswerPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.CommentRepository;
import com.balsamic.sejongmalsami.repository.postgres.DocumentRequestPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.MediaFileRepository;
import com.balsamic.sejongmalsami.repository.postgres.NoticePostRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.util.FileUtil;
import com.balsamic.sejongmalsami.util.ImageThumbnailGenerator;
import com.balsamic.sejongmalsami.util.MultipartFileAdapter;
import com.balsamic.sejongmalsami.util.config.FtpConfig;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import com.balsamic.sejongmalsami.util.storage.StorageService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 미디어 파일 서비스 클래스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MediaFileService {

  //  @Qualifier("ftpStorageService")
  private final StorageService storageService;
  private final MediaFileRepository mediaFileRepository;
  private final QuestionPostRepository questionPostRepository;
  private final AnswerPostRepository answerPostRepository;
  private final CommentRepository commentRepository;
  private final DocumentRequestPostRepository documentRequestPostRepository;
  private final NoticePostRepository noticePostRepository;
  private final FtpConfig ftpConfig;
  private final ImageThumbnailGenerator imageThumbnailGenerator;
  private static final Integer MAX_MEDIA_FILE_COUNT = 3;

  // 파일 유형별 최대 업로드 크기 (MB)
  private static final int MAX_BASIC_UPLOAD_SIZE = 50;    // 이미지, 문서 등
  private static final int MAX_VIDEO_UPLOAD_SIZE = 200;   // 비디오

  @Transactional
  public MediaFile saveFile(
      ContentType contentType,
      UUID postId,
      MultipartFile multipartFile) {

    // 파일 업로드 및 파일 Path 반환
    String filePath = storageService.uploadFile(contentType, multipartFile);

    // 썸네일 업로드 및 URL 생성
    String thumbnailUrl = generateThumbnailUrl(contentType, multipartFile);

    // 가공된 이미지 업로드 및 URL 생성 ( 압축된 화질이 좋은 URL 접근 가능한 이미지 )
    String uploadedImageUrl = storageService.uploadImage(contentType, multipartFile);

    // 게시글 검증
    if (contentType.equals(ContentType.QUESTION)) {
      QuestionPost questionPost = questionPostRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.DOCUMENT_POST_NOT_FOUND));
    } else if (contentType.equals(ContentType.ANSWER)) {
      AnswerPost answerPost = answerPostRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.ANSWER_POST_NOT_FOUND));
    } else if (contentType.equals(ContentType.COMMENT)) {
      Comment comment = commentRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
    } else if (contentType.equals(ContentType.DOCUMENT_REQUEST)) {
      DocumentRequestPost documentRequestPost = documentRequestPostRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.DOCUMENT_POST_NOT_FOUND));
    } else if (contentType.equals(ContentType.NOTICE)) {
      NoticePost noticePost = noticePostRepository.findById(postId)
          .orElseThrow(() -> new CustomException(ErrorCode.NOTICE_POST_NOT_FOUND));
    } else {
      log.error("지원하지않는 CONTENT TYPE 입니다");
      throw new CustomException(ErrorCode.INVALID_CONTENT_TYPE);
    }

    // 메타 데이터 MediaFile 저장
    MediaFile savedMediaFile = mediaFileRepository.save(
        MediaFile.builder()
            .postId(postId)
            .contentType(contentType)
            .originalFileName(multipartFile.getOriginalFilename())
            .thumbnailUrl(thumbnailUrl)
            .uploadedImageUrl(uploadedImageUrl)
            .uploadedFileName(FileUtil.extractFileName(filePath))
            .fileSize(multipartFile.getSize())
            .mimeType(MimeType.fromString(multipartFile.getContentType()))
            .filePath(filePath)
            .build());
    log.info("savedMediaFile 저장완료 : ID : {} ,업로드 파일명={}", savedMediaFile.getMediaFileId(), savedMediaFile.getUploadedFileName());
    return savedMediaFile;
  }


  //FIXME: 자네... 이것이 무엇이요~?
  /**
   * 질문게시글 또는 답변게시글에 파일 업로드
   *
   * @param postId 게시글 ID
   * @param files  업로드할 파일 리스트
   * @return 저장된 MediaFile 리스트
   */
  @Transactional
  public List<MediaFile> uploadMediaFiles(UUID postId, List<MultipartFile> files) {
    ContentType contentType = null;
    List<MediaFile> mediaFiles = new ArrayList<>();

    // 게시글 존재 여부 및 ContentType 결정
    if (questionPostRepository.existsById(postId)) {
      contentType = ContentType.QUESTION;
      // 해당 질문글 첨부파일이 3개를 초과했는지 체크
//      if (mediaFileRepository.countByPost(postId, ContentType.QUESTION) + files.size() > MAX_MEDIA_FILE_COUNT) { //2024.11.22: SUH :첨부파일 개수 초과 로직 오류로 주석처리
      if (files.size() > MAX_MEDIA_FILE_COUNT) {
        log.error("첨부파일 개수가 초과하였습니다. 현재 : {} , 최대 업로드할수있는 첨부파일 개수 : {}", files.size(), MAX_MEDIA_FILE_COUNT);
        throw new CustomException(ErrorCode.MEDIA_FILE_LIMIT_EXCEEDED);
      }
    } else if (answerPostRepository.existsById(postId)) {
      contentType = ContentType.ANSWER;
      // 해당 답변 첨부파일이 3개를 초과했는지 체크
//      if (mediaFileRepository.countByPost(postId, ContentType.ANSWER) + files.size() > MAX_MEDIA_FILE_COUNT) { //2024.11.22: SUH :첨부파일 개수 초과 로직 오류로 주석처리
      if (files.size() > MAX_MEDIA_FILE_COUNT) {
        log.error("첨부파일 개수가 초과하였습니다. 현재 : {} , 최대 업로드할수있는 첨부파일 개수 : {}", files.size(), MAX_MEDIA_FILE_COUNT);
        throw new CustomException(ErrorCode.MEDIA_FILE_LIMIT_EXCEEDED);
      }
    } else {
      log.error("uploadMediaFiles : 게시글 존재 여부 판단 하는 도중 오류 발생 : postId = {}", postId);
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }

    // 각 첨푸파일 처리
    for (MultipartFile file : files) {
      String mimeType = file.getContentType();

      // 첨부파일이 이미지 파일이 아닌 경우
      if (mimeType == null || !MimeType.isValidImageMimeType(mimeType)) {
        throw new CustomException(ErrorCode.INVALID_FILE_FORMAT);
      }

      // 파일 업로드
      String filePath = storageService.uploadFile(contentType, file);
      String thumbnailUrl = storageService.uploadThumbnail(contentType, file);

      MediaFile mediaFile = mediaFileRepository.save(MediaFile.builder()
          .postId(postId)
          .originalFileName(file.getOriginalFilename())
          .thumbnailUrl(thumbnailUrl)
          .filePath(filePath)
          .fileSize(file.getSize())
          .contentType(contentType)
          .mimeType(MimeType.fromString(mimeType))
          .build());

      mediaFiles.add(mediaFile);
      log.info("파일 저장 완료: 업로드 파일명={}", file.getOriginalFilename());
    }
    return mediaFiles;
  }

  /**
   * 업로드 파일이 이미지 타입인지 검증
   */
  private static boolean isValidImageFile(String mimeType) {
    return MimeType.isValidImageMimeType(mimeType);
  }

  /**
   * 파일 유효성 검증
   */
  public void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      log.error("업로드된 파일이 비어있습니다.");
      throw new CustomException(ErrorCode.FILE_EMPTY);
    }

    long fileSizeInMB = file.getSize() / (1024 * 1024);

    // 최대 파일 크기 검증
    if (fileSizeInMB > MAX_BASIC_UPLOAD_SIZE) {
      log.error("파일 크기가 초과되었습니다: 파일 크기={}MB, 최대 허용 크기={}MB", fileSizeInMB, MAX_BASIC_UPLOAD_SIZE);
      throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
    }

    String mimeType = file.getContentType();

    // MIME 타입 유효성 검증
    if (!MimeType.isValidMimeType(mimeType)) {
      log.error("유효하지 않은 MIME 타입: {}", mimeType);
      throw new CustomException(ErrorCode.INVALID_FILE_FORMAT);
    }

    // 이미지 MIME 타입 검증  (이미지가 아니면 에러)
    if (!MimeType.isValidImageMimeType(mimeType)) {
      log.error("MIME 타입이 이미지가 아닙니다: {}", mimeType);
      throw new CustomException(ErrorCode.INVALID_FILE_FORMAT);
    }

    log.info("파일 검증 성공: 파일명={}, MIME 타입={}, 크기={}MB", file.getOriginalFilename(), mimeType, fileSizeInMB);
  }

  /**
   * 썸네일 URL 생성
   *
   * @param contentType ContentType
   * @param file        대상 파일
   * @return 썸네일 URL
   */
  private String generateThumbnailUrl(ContentType contentType, MultipartFile file) {
    if (file.isEmpty()) {
      log.info("generateThumbnailUrl : 파일이 비어있습니다");
      return "";
    }
    try {
      // 썸네일 생성 및 업로드
      return createAndUploadThumbnail(contentType, file);
    } catch (Exception e) {
      log.error("썸네일 생성 실패: {}", e.getMessage(), e);
      // 업로드 타입에 따른 기본 썸네일 URL 사용
      return ftpConfig.getDefaultImageThumbnailUrl();
    }
  }

  /**
   * 썸네일 생성 및 업로드
   *
   * @param contentType ContentType
   * @param file        대상 파일
   * @return 업로드된 썸네일 URL
   */
  private String createAndUploadThumbnail(ContentType contentType, MultipartFile file) {
    byte[] thumbnailBytes;
    String mimeType = file.getContentType();

    // WebP 파일 처리 : MAC 환경에서는 WepP 를 다루지 않음 (라이브러리 지원안함)
    if (MimeType.WEBP.getMimeType().equalsIgnoreCase(mimeType)) {
      log.info("WebP 파일 처리: 원본 데이터 업로드");
      try {
        return storageService.uploadThumbnail(
            ContentType.THUMBNAIL,
            new MultipartFileAdapter(
                "thumbnail",
                imageThumbnailGenerator.generateThumbnailFileName(contentType, file.getOriginalFilename()),
                MimeType.WEBP.getMimeType(),
                file.getBytes()
            )
        );
      } catch (IOException e) {
        log.error("WebP 파일 원본 데이터 처리 중 오류: {}", e.getMessage(), e);
        throw new CustomException(ErrorCode.THUMBNAIL_CREATION_ERROR);
      }
    }

    // MIME 타입에 따라 썸네일 생성
    if (MimeType.isValidImageMimeType(mimeType)) {
      thumbnailBytes = imageThumbnailGenerator.generateImageThumbnail(file);
    } else if (MimeType.isValidDocumentMimeType(mimeType)) {
      thumbnailBytes = imageThumbnailGenerator.generateDocumentThumbnail(file);
    } else if (mimeType.startsWith("video/")) {
      thumbnailBytes = imageThumbnailGenerator.generateVideoThumbnail(file);
    } else {
      log.warn("지원되지 않는 MIME 타입: {}", mimeType);
      return "";
    }

    // 생성된 썸네일 파일 업로드
    String thumbnailFileName = imageThumbnailGenerator.generateThumbnailFileName(contentType,
        file.getOriginalFilename());
    MimeType thumbnailMimeType = imageThumbnailGenerator.getOutputThumbnailMimeType();
    MultipartFile thumbnailFile = new MultipartFileAdapter(
        "thumbnail",
        thumbnailFileName,
        thumbnailMimeType.getMimeType(),
        thumbnailBytes
    );

    log.debug("업로드 대상 썸네일 파일 이름: {}, MIME 타입: {}", thumbnailFileName, thumbnailMimeType.getMimeType());
    return storageService.uploadThumbnail(ContentType.THUMBNAIL, thumbnailFile);
  }
}
