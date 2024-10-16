package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.postgres.MediaFile;
import com.balsamic.sejongmalsami.object.MediaFileCommand;
import com.balsamic.sejongmalsami.object.MediaFileDto;
import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.constants.MimeType;
import com.balsamic.sejongmalsami.repository.postgres.AnswerPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.MediaFileRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.util.S3Service;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaFileService {

  private final S3Service s3Service;
  private final MediaFileRepository mediaFileRepository;
  private final QuestionPostRepository questionPostRepository;
  private final AnswerPostRepository answerPostRepository;
  private static final Integer MAX_MEDIA_FILE_COUNT = 3;

  // 질문게시글 파일 저장
  @Transactional
  public List<MediaFile> uploadMediaFiles(UUID postId, List<MultipartFile> files) {

    // 질문글 답변글 확인
    ContentType contentType = null;
    if (questionPostRepository.existsById(postId)) {
      contentType = ContentType.QUESTION;
      // 해당 질문글 첨부파일이 3개를 초과했는지 체크
      if (mediaFileRepository.countByPost(postId, ContentType.QUESTION) + files.size() > MAX_MEDIA_FILE_COUNT) {
        throw new CustomException(ErrorCode.MEDIA_FILE_LIMIT_EXCEEDED);
      }
    } else if (answerPostRepository.existsById(postId)) {
      contentType = ContentType.ANSWER;
      // 해당 답변 첨부파일이 3개를 초과했는지 체크
      if (mediaFileRepository.countByPost(postId, ContentType.ANSWER) + files.size() > MAX_MEDIA_FILE_COUNT) {
        throw new CustomException(ErrorCode.MEDIA_FILE_LIMIT_EXCEEDED);
      }
    } else {
      throw new CustomException(ErrorCode.INVALID_REQUEST);
    }

    List<MediaFile> mediaFiles = new ArrayList<>();

    for (MultipartFile file : files) {
      String mimeType = file.getContentType();
      // 첨부파일이 이미지 파일이 아닌 경우
      if (mimeType == null || !isInvalidImageFile(mimeType)) {
        throw new CustomException(ErrorCode.INVALID_FILE_FORMAT);
      }

      // S3에 파일 업로드
      mediaFiles.add(mediaFileRepository.save(MediaFile.builder()
          .postId(postId)
          .originalFileName(file.getOriginalFilename())
          .fileUrl(s3Service.uploadFile(file))
          .fileSize(file.getSize())
          .contentType(contentType)
          .mimeType(MimeType.fromString(mimeType))
          .build()));
    }
    return mediaFiles;
  }

  // 업로드 파일이 이미지 타입인지 검증
  private static boolean isInvalidImageFile(String mimeType) {
    return mimeType.equals("image/jpeg") || mimeType.equals("image/png");
  }
}
