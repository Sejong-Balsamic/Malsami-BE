package com.balsamic.sejongmalsami.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class S3Service {

    private final AmazonS3Client amazonS3Client;

    @Value("${S3_BUCKET_NAME}")
    private String bucketName;

    public String uploadFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename(); // 원본 파일 명
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")); // 확장자 명

        // 파일 확장자 검증
        if (!Arrays.asList(".jpg", ".png", ".mp4", ".zip", ".jpeg", ".avi", ".mov", ".mp3", ".wav", ".aac").contains(extension.toLowerCase())) {
            throw new CustomException(ErrorCode.INVALID_FILE_FORMAT);
        }

        String fileUrl = "https://" + bucketName + UUID.randomUUID() + "_" + originalFilename;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        amazonS3Client.putObject(bucketName, originalFilename, file.getInputStream(), metadata);

        return fileUrl;
    }
}
