package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.postgres.AnswerPost;
import com.balsamic.sejongmalsami.object.postgres.Comment;
import com.balsamic.sejongmalsami.object.postgres.DocumentFile;
import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.object.postgres.DocumentRequestPost;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.repository.postgres.AnswerPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.DocumentFileRepository;
import com.balsamic.sejongmalsami.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.DocumentRequestPostRepository;
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
  private final DocumentPostRepository documentPostRepository;
  private final DocumentFileRepository documentFileRepository;
  private final DocumentRequestPostRepository documentRequestPostRepository;

  private final Random random = new Random();

  /**
   * <h3>질문 글 Mock 데이터 생성 및 답변 글 동시 생성</h3>
   * <p>지정된 개수만큼의 질문 글을 생성하고, 각 질문 글에 대해 0개에서 10개 사이의 답변 글을 생성합니다.
   * 답변 글의 작성자는 질문 글 작성자와 다르며, 각 질문 글에 대해 단 하나의 답변 글만 채택될 수 있습니다.</p>
   *
   * @param postCount 생성할 질문 글의 총 개수
   */
  @Transactional
  public void createMockQuestionPostAndAnswerPost(Integer postCount) {

    // 잘못된 값 입력시 기본 30개 설정
    if (postCount == null || postCount <= 0) {
      log.warn("잘못된 작성개수가 입력되었습니다. {} 기본 값 30개로 설정합니다.", postCount);
      postCount = 30;
    }

    int questionTotalCreated = 0;
    int userCount = 0;
    Random random = new Random();

    while (questionTotalCreated < postCount) {
      // 1. Mock 사용자 생성
      Member questionMember = testDataGenerator.createMockMember();
      userCount++;

      // 2. 생성할 질문글 수 결정 (1 ~ 10개)
      int questionRemaining = postCount - questionTotalCreated;
      int numQuestions = random.nextInt(10) + 1; // 1 ~ 10
      numQuestions = Math.min(numQuestions, questionRemaining); // 남은 수보다 많지 않도록 조정

      // 3. 질문글 생성
      for (int i = 0; i < numQuestions; i++) {
        QuestionPost questionPost = testDataGenerator.createMockQuestionPost(questionMember);
        questionTotalCreated++;

        // 4. 질문글에 댓글 생성 (0 ~ 5개)
        int numComments = random.nextInt(6); // 0 ~ 5
        for (int j = 0; j < numComments; j++) {
          // 댓글 작성자 생성
          Member commentWriter = testDataGenerator.createMockMember();
          Comment comment = testDataGenerator.createMockComment(
              commentWriter,
              questionPost.getQuestionPostId(),
              ContentType.QUESTION
          );
        }

        // 5. 답변 글 생성 (0 ~ 10개)
        int numAnswers = random.nextInt(11); // 0 ~ 10
        List<AnswerPost> answerPosts = new ArrayList<>();

        for (int j = 0; j < numAnswers; j++) {
          // 답변 작성자 생성
          Member answerWriter = testDataGenerator.createMockMember();
          AnswerPost answerPost = testDataGenerator.createMockAnswerPost(answerWriter, questionPost);
          answerPosts.add(answerPost);

          // 6. 답변글에 댓글 생성 (0 ~ 5개)
          numComments = random.nextInt(6); // 0 ~ 5
          for (int k = 0; k < numComments; k++) {
            // 댓글 작성자 생성
            Member commentWriter = testDataGenerator.createMockMember();
            Comment comment = testDataGenerator.createMockComment(
                commentWriter,
                answerPost.getAnswerPostId(),
                ContentType.ANSWER
            );
          }
        }

        // 7. 답변 채택
        if (!answerPosts.isEmpty()) {
          int chaetaekIndex = random.nextInt(answerPosts.size());
          // index가 홀수인 경우만 채택 (채택 안된글도 존재해야하므로)
          if (chaetaekIndex % 2 != 0) {
            AnswerPost chaetaekAnswer = answerPosts.get(chaetaekIndex);
            chaetaekAnswer.markAsChaetaek();
            // 답변이 채택된 질문글도 chaetaekStatus true로 변경
            questionPost.markAsChaetaek();
            questionPostRepository.save(questionPost);
            answerPostRepository.save(chaetaekAnswer);
          }
        }

        // 8. 답변 수 동기화
        questionPost.updateAnswerCount(answerPosts.size());
        questionPostRepository.save(questionPost);
      }
    }

    log.info("총 {} 명의 mock 유저가 {} 개의 mock 질문글을 생성했습니다.",
        userCount, questionTotalCreated);
  }

  /**
   * <h3>DocumentPost 및 관련 DocumentFile Mock 데이터 생성</h3>
   * <p>지정된 개수만큼의 DocumentPost를 생성하고, 각 DocumentPost에 대해 0개에서 5개 사이의 DocumentFile을 생성합니다.
   * 회원 풀을 미리 생성하여 게시물 작성 시 이들 중에서 랜덤으로 선택합니다.</p>
   *
   * @param postCount 생성할 DocumentPost의 총 개수
   */
  @Transactional
  public void createMockDocumentPostAndDocumentFiles(Integer postCount) {
    // 잘못된 값 입력 시 기본 30개 설정
    if (postCount == null || postCount <= 0) {
      log.warn("잘못된 작성 개수가 입력되었습니다. {} 기본 값 30개로 설정합니다.", postCount);
      postCount = 30;
    }

    // 1. 회원 풀 생성 (postCount보다 작게, 예: 50명 또는 postCount의 10%)
    int memberPoolSize = Math.min(50, Math.max(10, postCount / 10));
    List<Member> memberPool = new ArrayList<>();
    for (int i = 0; i < memberPoolSize; i++) {
      Member member = testDataGenerator.createMockMember();
      memberPool.add(member);
    }
    log.info("회원 풀 생성 완료: {}명", memberPool.size());

    int documentPostTotalCreated = 0;
    int userCount = memberPool.size();

    while (documentPostTotalCreated < postCount) {
      // 2. 생성할 DocumentPost 수 결정 (1 ~ 10개)
      int documentRemaining = postCount - documentPostTotalCreated;
      int numDocuments = random.nextInt(10) + 1; // 1 ~ 10
      numDocuments = Math.min(numDocuments, documentRemaining); // 남은 수보다 많지 않도록 조정

      // 3. DocumentPost 생성 및 관련 DocumentFile 생성
      for (int i = 0; i < numDocuments; i++) {
        // 3.1 회원 풀에서 랜덤으로 작성자 선택
        Member member = memberPool.get(random.nextInt(memberPool.size()));

        // 3.2 DocumentPost 생성
        DocumentPost documentPost = testDataGenerator.createMockDocumentPost(member);
        documentPostTotalCreated++;

        // 3.3 DocumentFile 생성 (0 ~ 5개)
        int numFiles = random.nextInt(6); // 0 ~ 5
        List<DocumentFile> documentFiles = new ArrayList<>();

        for (int j = 0; j < numFiles; j++) {
          // 파일 업로더는 회원 풀에서 랜덤으로 선택
          Member uploader = memberPool.get(random.nextInt(memberPool.size()));
          DocumentFile documentFile = testDataGenerator.createMockDocumentFile(uploader, documentPost);
          documentFiles.add(documentFile);
        }
      }
    }

    log.info("총 {} 명의 mock 유저가 {} 개의 mock DocumentPost를 생성했습니다.",
        userCount, documentPostTotalCreated);
  }

  /**
   * <h3>자료 요청 글 Mock 데이터 생성</h3>
   * <p>지정된 개수만큼의 자료 요청 글을 생성합니다.
   * <p>생성된 자료요청글에 0~5개의 댓글을 작성합니다.</p>
   *
   * @param postCount 생성할 자료 요청 글의 총 개수
   */
  @Transactional
  public void createMockDocumentRequestPost(Integer postCount) {

    // 잘못된 값 입력시 기본 30개 설정
    if (postCount == null || postCount <= 0) {
      log.warn("잘못된 작성개수가 입력되었습니다. {} 기본 값 30개로 설정합니다.", postCount);
      postCount = 30;
    }

    int totalCreated = 0;
    int userCount = 0;
    Random random = new Random();

    while (totalCreated < postCount) {
      // 1. Mock 사용자 생성
      Member postWriter = testDataGenerator.createMockMember();
      userCount++;

      // 2. 생성할 자료 요청 글 수 결정 (1 ~ 10개)
      int postRemaining = postCount - totalCreated;
      int numPosts = random.nextInt(10) + 1; // 1 ~ 10
      numPosts = Math.min(numPosts, postRemaining); // 남은 수보다 많지 않도록 조정

      // 3. 자료 요청 글 생성
      for (int i = 0; i < numPosts; i++) {
        DocumentRequestPost documentRequestPost = testDataGenerator.createMockDocumentRequestPost(postWriter);
        documentRequestPostRepository.save(documentRequestPost);
        totalCreated++;

        // 4. 댓글 작성
        int numComments = random.nextInt(6); // 0 ~ 5
        for (int j = 0; j < numComments; j++) {
          // 댓글 작성자 생성
          Member commentWriter = testDataGenerator.createMockMember();
          Comment comment = testDataGenerator.createMockComment(
              commentWriter,
              documentRequestPost.getDocumentRequestPostId(),
              ContentType.DOCUMENT_REQUEST
          );
        }
      }
    }

    log.info("총 {} 명의 mock 유저가 {} 개의 자료 요청 글을 생성했습니다.",
        userCount, totalCreated);
  }
}
