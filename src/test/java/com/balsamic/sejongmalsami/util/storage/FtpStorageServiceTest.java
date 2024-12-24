package com.balsamic.sejongmalsami.util.storage;

import static com.balsamic.sejongmalsami.util.LogUtil.lineLog;

import com.balsamic.sejongmalsami.object.constants.ContentType;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
class FtpStorageServiceTest {
  @Autowired
  FtpStorageService ftpStorageService;

  @Test
  void mainTest() throws IOException {
    uploadFile();
  }

  void uploadFile() throws IOException {
    String path = "src/test/resources/document/테스트_문서_PDF.pdf";
    File file = new File(path);
    MultipartFile multipartFile = new MockMultipartFile(
        "file",
        file.getName(),
        MediaType.APPLICATION_PDF_VALUE,
        new FileInputStream(file)
    );

    String uploadFilePath = ftpStorageService.uploadFile(ContentType.DOCUMENT, multipartFile);
    lineLog(uploadFilePath);
  }
}