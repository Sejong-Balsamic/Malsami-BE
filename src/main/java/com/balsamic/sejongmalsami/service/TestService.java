package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.postgres.AnswerPost;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.repository.postgres.AnswerPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.util.TestDataGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestService {

  private final TestDataGenerator testDataGenerator;
  private final QuestionPostRepository questionPostRepository;
  private final AnswerPostRepository answerPostRepository;

  /**
   * <h3>질문 글 Mock 데이터 생성 및 답변 글 동시 생성</h3>
   * <p>지정된 개수만큼의 질문 글을 생성하고, 각 질문 글에 대해 0개에서 10개 사이의 답변 글을 생성합니다.
   * 답변 글의 작성자는 질문 글 작성자와 다르며, 각 질문 글에 대해 단 하나의 답변 글만 채택될 수 있습니다.</p>
   *
   * @param questionPostCount 생성할 질문 글의 총 개수
   */
  @Transactional
  public void createMockQuestionPostAndAnswerPost(Integer questionPostCount) {

    // 잘못된 값 입력시 기본 30개 설정
    if (questionPostCount == null || questionPostCount <= 0) {
      log.warn("잘못된 작성개수가 입력되었습니다. {} 기본 값 30개로 설정합니다.", questionPostCount);
      questionPostCount = 30;
    }

    int questionTotalCreated = 0;
    int userCount = 0;
    Random random = new Random();

    while (questionTotalCreated < questionPostCount) {
      // 1. Mock 사용자 생성
      Member questionMember = testDataGenerator.createMockMember();
      userCount++;

      // 2. 생성할 질문글 수 결정 (1 ~ 10개)
      int questionRemaining = questionPostCount - questionTotalCreated;
      int numQuestions = random.nextInt(10) + 1; // 1 ~ 10
      numQuestions = Math.min(numQuestions, questionRemaining); // 남은 수보다 많지 않도록 조정

      // 3. 질문글 생성
      for (int i = 0; i < numQuestions; i++) {
        QuestionPost questionPost = testDataGenerator.createMockQuestionPost(questionMember);
        questionTotalCreated++;

        // 4. 답변 글 생성 (0 ~ 10개)
        int numAnswers = random.nextInt(11); // 0 ~ 10
        List<AnswerPost> answerPosts = new ArrayList<>();

        for (int j = 0; j < numAnswers; j++) {
          // 답변 작성자 생성
          Member answerWriter = testDataGenerator.createMockMember();
          AnswerPost answerPost = testDataGenerator.createMockAnswerPost(answerWriter, questionPost);
          answerPosts.add(answerPost);
        }

        // 5. 답변 채택
        if (!answerPosts.isEmpty()) {
          int chaetaekIndex = random.nextInt(answerPosts.size());
          // index가 홀수인 경우만 채택 (채택 안된글도 존재해야하므로)
          if (chaetaekIndex % 2 != 0) {
            AnswerPost chaetaekAnswer = answerPosts.get(chaetaekIndex);
            chaetaekAnswer.markAsChaetaek();
            answerPostRepository.save(chaetaekAnswer);
          }
        }

        // 6. 답변 수 동기화
        questionPost.updateAnswerCount(answerPosts.size());
        questionPostRepository.save(questionPost);
      }
    }

    log.info("총 {} 명의 mock 유저가 {} 개의 mock 질문글을 생성했습니다.",
        userCount, questionTotalCreated);
  }
}
