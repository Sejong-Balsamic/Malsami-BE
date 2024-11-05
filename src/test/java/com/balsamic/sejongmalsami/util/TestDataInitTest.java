package com.balsamic.sejongmalsami.util;

import static com.balsamic.sejongmalsami.util.LogUtils.*;
import static org.junit.jupiter.api.Assertions.*;

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
class TestDataInitTest {
  @Autowired
  TestDataInit testDataInit;

  @Test
  public void mainTest() {
//    createMember();
//    createDocumentPost();
    createDocumentFile();
  }

  void createMember() {
    Member member = testDataInit.createMember();
    superLog(member);
  }

  void createDocumentPost() {
    Member member = testDataInit.createMember();
    superLog(member);

    DocumentPost documentPost = testDataInit.createDocumentPost(member);
    superLog(documentPost);
  }

  void createDocumentFile() {
    Member member = testDataInit.createMember();
    superLog(member);

    DocumentPost documentPost = testDataInit.createDocumentPost(member);
    superLog(documentPost);

    DocumentFile documentFile = testDataInit.createDocumentFile(documentPost, member);
    superLog(documentFile);
  }
}