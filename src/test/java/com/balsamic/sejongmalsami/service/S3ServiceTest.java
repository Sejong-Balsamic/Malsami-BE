package com.balsamic.sejongmalsami.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
class S3ServiceTest {

    @Mock
    private AmazonS3Client amazonS3Client;

    @InjectMocks
    private S3Service s3Service;

    private final String bucketName = "sejong-malsami";

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(s3Service, "bucketName", bucketName);  // 버킷 이름을 수동으로 주입
    }

    @Test
    void 업로드() throws IOException {
        // Mock MultipartFile 생성
        String fileName = "testfile.png";
        String contentType = "image/png";
        byte[] content = "dummy content".getBytes();
        MultipartFile mockFile = new MockMultipartFile(fileName, fileName, contentType, content);

        // AmazonS3Client의 putObject 메서드가 정상적으로 호출되는지 확인
        when(amazonS3Client.putObject(eq(bucketName), eq(fileName), any(ByteArrayInputStream.class), any(ObjectMetadata.class)))
                .thenReturn(null);

        // 업로드 후 예상되는 URL
        String expectedUrl = "https://" + bucketName + "/" + UUID.randomUUID() + "_" + fileName;

        // 테스트 대상 메서드 호출
        String actualUrl = s3Service.uploadFile(mockFile);

        // 예상 URL이 실제 반환된 URL에 포함되어 있는지 확인
        Assertions.assertTrue(actualUrl.contains(fileName));

        // putObject 메서드가 한 번 호출되었는지 확인
        verify(amazonS3Client).putObject(eq(bucketName), eq(fileName), any(ByteArrayInputStream.class), any(ObjectMetadata.class));

        log.info("------------------------");
        log.info("bucketName = {}", bucketName);
        log.info("expectedUrl = {}", expectedUrl);
        log.info("actualUrl = {}", actualUrl);
        log.info("------------------------");
    }
}