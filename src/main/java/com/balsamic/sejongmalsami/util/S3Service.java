package com.balsamic.sejongmalsami.util;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.balsamic.sejongmalsami.object.constants.MimeType;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3Service {

  private final AmazonS3Client amazonS3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  public String uploadFile(MultipartFile file) {
    String originalFilename = file.getOriginalFilename(); // 원본 파일 명
    String contentType = file.getContentType();

    // MIME Type 검증
    if (contentType == null || !MimeType.isValidMimeType(contentType)) {
      throw new CustomException(ErrorCode.INVALID_FILE_FORMAT);
    }

    String fileName = UUID.randomUUID() + "_" + originalFilename;

    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(file.getContentType());
    metadata.setContentLength(file.getSize());

    try {
      amazonS3Client.putObject(bucketName, fileName, file.getInputStream(), metadata);
    } catch (IOException e) {
      log.error("업로드 파일명 = {}", originalFilename);
      throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
    }
    return amazonS3Client.getUrl(bucketName, fileName).toString();
  }
}
