package com.balsamic.sejongmalsami.web.service;

import com.balsamic.sejongmalsami.application.test.TestDataGenerator;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.post.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.web.SejongMalsamiBackendApplication;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = SejongMalsamiBackendApplication.class)
@ActiveProfiles("dev")
@Slf4j
@Transactional
class CommentServiceTest {

  @Autowired
  TestDataGenerator testDataGenerator;

  @Test
  public void mainTest() {
    addComment();
  }

  public void addComment() {
    Member mockMember = testDataGenerator.createMockMember();
    QuestionPost mockQuestionPost = testDataGenerator.createMockQuestionPost(mockMember);

    Member mockMember2 = testDataGenerator.createMockMember();
    //TODO: 구현하다 맘
  }

}