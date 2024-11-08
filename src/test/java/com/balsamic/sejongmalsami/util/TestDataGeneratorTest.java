package com.balsamic.sejongmalsami.util;

import static com.balsamic.sejongmalsami.util.LogUtils.*;

import com.balsamic.sejongmalsami.object.postgres.DocumentFile;
import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.object.postgres.Member;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
@Transactional
class TestDataGeneratorTest {
  @Autowired
  TestDataGenerator testDataGenerator;

  @Test
  public void mainTest() {
    createMember();
    createDocumentPost();
    createDocumentFile();
  }

  void createMember() {
    Member member = testDataGenerator.createMockMember();
    superLog(member);
  }

  void createDocumentPost() {
    Member member = testDataGenerator.createMockMember();
    superLog(member);

    DocumentPost documentPost = testDataGenerator.createMockDocumentPost(member);
    superLog(documentPost);
  }

  void createDocumentFile() {
    Member member = testDataGenerator.createMockMember();
    superLog(member);

    DocumentPost documentPost = testDataGenerator.createMockDocumentPost(member);
    superLog(documentPost);

    DocumentFile documentFile = testDataGenerator.createMockDocumentFile(member,documentPost);
    superLog(documentFile);
  }
}