package com.balsamic.sejongmalsami.util;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.balsamic.sejongmalsami.object.constants.ExtensionType;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class S3Service {

  private final AmazonS3Client amazonS3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  public String uploadFile(MultipartFile file) throws IOException {
    String originalFilename = file.getOriginalFilename(); // 원본 파일 명
    String extension = originalFilename.substring(originalFilename.lastIndexOf(".")); // 확장자 명

    // 파일 확장자 검증
    if (!ExtensionType.isValidExtension(extension)) {
      throw new CustomException(ErrorCode.INVALID_FILE_FORMAT);
    }

    String fileName = bucketName + "/" + UUID.randomUUID() + "_" + originalFilename;

    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(file.getContentType());
    metadata.setContentLength(file.getSize());

    amazonS3Client.putObject(bucketName, fileName, file.getInputStream(), metadata);

    return fileName;
  }
}
