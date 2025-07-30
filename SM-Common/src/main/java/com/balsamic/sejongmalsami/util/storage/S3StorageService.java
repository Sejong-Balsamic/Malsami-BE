package com.balsamic.sejongmalsami.util.storage;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.balsamic.sejongmalsami.constants.ContentType;
import com.balsamic.sejongmalsami.constants.MimeType;
import com.balsamic.sejongmalsami.util.FileUtil;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service("s3StorageService")
@Slf4j
@RequiredArgsConstructor
public class S3StorageService implements StorageService {

  private final AmazonS3Client amazonS3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  @Override
  public String uploadFile(ContentType contentType, MultipartFile file) {
    String originalFilename = file.getOriginalFilename();
    if (originalFilename == null) {
      throw new CustomException(ErrorCode.INVALID_FILE_FORMAT);
    }

    // 파일명 생성
    String fileName = FileUtil.generateFileName(contentType, originalFilename);

    // MIME Type 검증
    if (!MimeType.isValidMimeType(contentType.name())) {
      throw new CustomException(ErrorCode.INVALID_FILE_FORMAT);
    }

    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(contentType.name());
    metadata.setContentLength(file.getSize());

    try {
      amazonS3Client.putObject(bucketName, fileName, file.getInputStream(), metadata);
      log.info("S3 파일 업로드 성공: {}", fileName);
    } catch (IOException e) {
      log.error("S3 파일 업로드 실패: {}", originalFilename);
      throw new CustomException(ErrorCode.S3_FILE_UPLOAD_ERROR);
    }

    return amazonS3Client.getUrl(bucketName, fileName).toString();
  }

  @Override
  public String uploadThumbnail(ContentType contentType, MultipartFile file) {
    //TODO: 미구현
    return "미구현";
  }

  @Override
  public String uploadImage(ContentType contentType, MultipartFile file) {
    //TODO: 미구현
    return "미구현";
  }

  @Override
  public void deleteFile(ContentType contentType, String fileUrl) {
    // 현재 로직에서 S3Storage는 ContentType을 사용하지 않습니다
    String key = extractKeyFromUrl(fileUrl);
    try {
      amazonS3Client.deleteObject(bucketName, key);
      log.info("S3 파일 삭제 성공: {}", key);
    } catch (Exception e) {
      log.error("S3 파일 삭제 실패: {}", key);
      throw new CustomException(ErrorCode.S3_FILE_DELETE_ERROR);
    }
  }

  /**
   * S3 URL에서 키를 추출
   */
  private String extractKeyFromUrl(String fileUrl) {
    return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
  }
}
