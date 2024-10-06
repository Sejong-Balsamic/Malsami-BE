//package com.balsamic.sejongmalsami.service;
//
//import com.balsamic.sejongmalsami.object.MediaFile;
//import com.balsamic.sejongmalsami.object.MediaFileCommand;
//import com.balsamic.sejongmalsami.object.MediaFileDto;
//import com.balsamic.sejongmalsami.object.QuestionPost;
//import com.balsamic.sejongmalsami.object.constants.ExtensionType;
//import com.balsamic.sejongmalsami.repository.postgres.AnswerPostRepository;
//import com.balsamic.sejongmalsami.repository.postgres.MediaFileRepository;
//import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
//import com.balsamic.sejongmalsami.util.S3Service;
//import com.balsamic.sejongmalsami.util.exception.CustomException;
//import com.balsamic.sejongmalsami.util.exception.ErrorCode;
//import java.io.IOException;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class MediaFileService {
//
//  private final S3Service s3Service;
//  private final MediaFileRepository mediaFileRepository;
//  private final QuestionPostRepository questionPostRepository;
//  private final AnswerPostRepository answerPostRepository;
//  private static final Integer MAX_MEDIA_FILE_COUNT = 3;
//  private String fileUrl = null;
//
//  // 질문게시글 파일 저장
//  @Transactional
//  public MediaFileDto uploadMediaFileForQuestionPost(MediaFileCommand command) {
//    QuestionPost questionPost = questionPostRepository.findById(command.getQuestionPostId())
//        .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_POST_NOT_FOUND));
//
//    String extensionType = s3Service.getExtensionType(command.getFile());
//
//    // 첨부파일이 3개를 초과하는지 체크
//    if (mediaFileRepository.countByQuestionPost(questionPost) > MAX_MEDIA_FILE_COUNT) {
//      throw new CustomException(ErrorCode.MEDIA_FILE_LIMIT_EXCEEDED);
//    }
//
//    // 첨부파일이 이미지 파일이 아닌경우
//    if (isInvalidImageExtension(extensionType)) {
//      throw new CustomException(ErrorCode.INVALID_FILE_FORMAT);
//    }
//
//    // S3에 파일 업로드
//    try {
//      fileUrl = s3Service.uploadFile(command.getFile());
//    } catch (IOException e) {
//      throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
//    }
//
//    MediaFile mediaFile = MediaFile.builder()
//        .questionPost(questionPost)
//        .fileUrl(fileUrl)
//        .fileSize(command.getFile().getSize())
//        .fileType(ExtensionType.valueOfExtension(extensionType))
//        .build();
//
//    log.info("MediaFile: filePath={}, fileSize={}, fileType={}",
//        mediaFile.getFileUrl(),
//        mediaFile.getFileSize(),
//        mediaFile.getFileType());
//
//    return MediaFileDto.builder()
//        .mediaFile(mediaFileRepository.save(mediaFile))
//        .build();
//  }
//
//  private static boolean isInvalidImageExtension(String extensionType) {
//    return !extensionType.equals("jpg") && !extensionType.equals("png") && !extensionType.equals("jpeg");
//  }
//
//
//}
