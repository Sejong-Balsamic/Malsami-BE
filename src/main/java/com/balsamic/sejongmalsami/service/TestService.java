package com.balsamic.sejongmalsami.service;

import com.amazonaws.util.IOUtils;
import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.TestCommand;
import com.balsamic.sejongmalsami.object.TestDto;
import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.postgres.AnswerPost;
import com.balsamic.sejongmalsami.object.postgres.Comment;
import com.balsamic.sejongmalsami.object.postgres.DocumentFile;
import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.object.postgres.DocumentRequestPost;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.object.postgres.Subject;
import com.balsamic.sejongmalsami.repository.postgres.AnswerPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.DocumentFileRepository;
import com.balsamic.sejongmalsami.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.DocumentRequestPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.SubjectRepository;
import com.balsamic.sejongmalsami.util.TestDataGenerator;
import com.balsamic.sejongmalsami.util.TimeUtil;
import com.balsamic.sejongmalsami.util.WebDriverManager;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestService {

  private final TestDataGenerator testDataGenerator;
  private final QuestionPostRepository questionPostRepository;
  private final QuestionPostService questionPostService;
  private final AnswerPostRepository answerPostRepository;
  private final DocumentPostRepository documentPostRepository;
  private final DocumentFileRepository documentFileRepository;
  private final DocumentRequestPostRepository documentRequestPostRepository;
  private final GenericObjectPool<FTPClient> ftpClientPool;
  private final SubjectRepository subjectRepository;
  private final WebDriverManager webDriverManager; // 의존성 주입

  private final Random random = new Random();
  private final Set<String> processedDocIds = new HashSet<>();
  private static final ObjectMapper objectMapper = new ObjectMapper();

  // ExecutorService를 사용하여 비동기 작업 관리
//  private final ExecutorService executorService = Executors.newFixedThreadPool(10);

  /**
   * WebDriver 종료
   */
  @PreDestroy
  public void shutdown() {
//    executorService.shutdown();
    webDriverManager.quitDriver();
  }

  /**
   * 비동기적으로 Mock 질문글 생성 작업 실행
   */
//  public void executeAsyncCreateMockQuestionPost(TestCommand command) {
//    executorService.submit(() -> {
//      try {
//        createMockQuestionPostAndAnswerPost(command);
//      } finally {
//        // 작업이 끝난 후 WebDriver 종료
//        webDriverManager.quitDriver();
//      }
//    });
//  }

  /**
   * 질문 글 Mock 데이터 생성 및 답변 글 동시 생성
   * 지정된 개수만큼의 질문 글을 생성하고, 각 질문 글에 대해 0개에서 10개 사이의 답변 글을 생성합니다.
   * 답변 글의 작성자는 질문 글 작성자와 다르며, 각 질문 글에 대해 단 하나의 답변 글만 채택될 수 있습니다.
   * 답변 및 댓글 작성자는 회원 풀을 미리 생성한 뒤 랜덤으로 작성자를 선택합니다.
   */
  @Transactional
  public void createMockQuestionPostAndAnswerPost(TestCommand command) {
    Integer postCount = command.getPostCount();

    // 잘못된 값 입력 시 기본값 설정
    if (postCount == null || postCount <= 0) {
      log.warn("잘못된 작성 개수가 입력되었습니다. 기본 값 10개로 설정합니다.");
      postCount = 10;
    }

    // 답변 및 댓글 작성자 풀 생성
    List<Member> memberPool = createMemberPool(postCount);

    int questionTotalCreated = 0;
    int userCount = 0;

    while (questionTotalCreated < postCount) {
      // 작성자 결정 (useMockMember 값에 따라 실제 Member 또는 가짜 Member 선택)
      Member questionMember;
      if (command.isUseMockMember()) {
        questionMember = testDataGenerator.createMockMember(); // 가짜 Member
      } else {
        questionMember = command.getMember(); // 실제 Member
      }
      userCount++;

      // 생성할 질문글 수 결정
      int questionRemaining = postCount - questionTotalCreated;
      int numQuestions = Math.min(random.nextInt(10) + 1, questionRemaining);

      // 질문글 생성
      for (int i = 0; i < numQuestions; i++) {
        QuestionPost questionPost = testDataGenerator.createMockQuestionPost(questionMember);
        questionTotalCreated++;

        // 질문글에 댓글 생성 (0 ~ 5개)
        int numComments = random.nextInt(6);
        for (int j = 0; j < numComments; j++) {
          Member commentWriter = memberPool.get(random.nextInt(memberPool.size()));
          Comment comment = testDataGenerator.createMockComment(
              commentWriter,
              questionPost.getQuestionPostId(),
              ContentType.QUESTION
          );
          questionPost.increaseCommentCount();
        }

        // 답변 글 생성 (0 ~ 10개)
        int numAnswers = random.nextInt(11);
        List<AnswerPost> answerPosts = new ArrayList<>();
        for (int j = 0; j < numAnswers; j++) {
          Member answerWriter = memberPool.get(random.nextInt(memberPool.size()));
          AnswerPost answerPost = testDataGenerator.createMockAnswerPost(answerWriter, questionPost);
          answerPosts.add(answerPost);

          // 답변글에 댓글 생성 (0 ~ 5개)
          int answerComments = random.nextInt(6);
          for (int k = 0; k < answerComments; k++) {
            Member commentWriter = memberPool.get(random.nextInt(memberPool.size()));
            Comment comment = testDataGenerator.createMockComment(
                commentWriter,
                answerPost.getAnswerPostId(),
                ContentType.ANSWER
            );
            answerPost.increaseCommentCount();
          }
        }

        // 답변 채택
        if (!answerPosts.isEmpty()) {
          int chaetaekIndex = random.nextInt(answerPosts.size());
          if (chaetaekIndex % 2 != 0) {
            AnswerPost chaetaekAnswer = answerPosts.get(chaetaekIndex);
            chaetaekAnswer.markAsChaetaek();
            questionPost.setChaetaekStatus(true);
            questionPostRepository.save(questionPost);
            answerPostRepository.save(chaetaekAnswer);
          }
        }

        // 답변 수 동기화
        questionPost.setAnswerCount(answerPosts.size());
        questionPostRepository.save(questionPost);
      }
    }

    log.info("총 {} 명의 mock 유저가 {} 개의 mock 질문글을 생성했습니다.", userCount, questionTotalCreated);
  }

  /**
   * DocumentPost 및 관련 DocumentFile Mock 데이터 생성
   * 지정된 개수만큼의 DocumentPost를 생성하고, 각 DocumentPost에 대해 0개에서 5개 사이의 DocumentFile을 생성합니다.
   * 회원 풀을 미리 생성하여 게시물 작성 시 이들 중에서 랜덤으로 선택합니다.
   */
  @Transactional
  public void createMockDocumentPostAndDocumentFiles(TestCommand command) {
    Integer postCount = command.getPostCount();
    // 잘못된 값 입력 시 기본 30개 설정
    if (postCount == null || postCount <= 0) {
      log.warn("잘못된 작성 개수가 입력되었습니다. {} 기본 값 30개로 설정합니다.", postCount);
      postCount = 30;
    }

    // 1. 회원 풀 생성 (postCount보다 작게, 예: 50명 또는 postCount의 10%)
    List<Member> memberPool = createMemberPool(postCount);
    log.info("회원 풀 생성 완료: {}명", memberPool.size());

    int documentPostTotalCreated = 0;
    int userCount = memberPool.size();

    while (documentPostTotalCreated < postCount) {
      // 생성할 DocumentPost 수 결정 (1 ~ 10개)
      int documentRemaining = postCount - documentPostTotalCreated;
      int numDocuments = random.nextInt(10) + 1; // 1 ~ 10
      numDocuments = Math.min(numDocuments, documentRemaining); // 남은 수보다 많지 않도록 조정

      // DocumentPost 생성 및 관련 DocumentFile 생성
      for (int i = 0; i < numDocuments; i++) {
        // 회원 풀에서 랜덤으로 작성자 선택
        Member member = memberPool.get(random.nextInt(memberPool.size()));

        // DocumentPost 생성
        DocumentPost documentPost = testDataGenerator.createMockDocumentPost(member);
        documentPostTotalCreated++;

        // 댓글 작성
        int numComments = random.nextInt(11); // 0 ~ 10
        for (int j = 0; j < numComments; j++) {
          // 회원 풀에서 랜덤으로 댓글 작성자 선택
          Member commentWriter = memberPool.get(random.nextInt(memberPool.size()));
          Comment comment = testDataGenerator.createMockComment(
              commentWriter,
              documentPost.getDocumentPostId(),
              ContentType.DOCUMENT
          );
          documentPost.increaseCommentCount();
        }

        // DocumentFile 생성 (0 ~ 5개)
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
   * 자료 요청 글 Mock 데이터 생성
   * 지정된 개수만큼의 자료 요청 글을 생성합니다.
   * 생성된 자료요청글에 0~5개의 댓글을 작성합니다.
   * 댓글 작성자는 회원 풀을 미리 생성하여 댓글 작성 시 랜덤으로 작성자를 선택합니다.
   */
  @Transactional
  public void createMockDocumentRequestPost(TestCommand command) {
    Integer postCount = command.getPostCount();

    // 잘못된 값 입력시 기본 30개 설정
    if (postCount == null || postCount <= 0) {
      log.warn("잘못된 작성개수가 입력되었습니다. {} 기본 값 30개로 설정합니다.", postCount);
      postCount = 30;
    }
    // 댓글 작성자 풀 생성
    List<Member> memberPool = createMemberPool(postCount);

    int totalCreated = 0;
    int userCount = 0;
    Random random = new Random();

    while (totalCreated < postCount) {
      // Mock 사용자 생성
      Member postWriter = testDataGenerator.createMockMember();
      userCount++;

      // 생성할 자료 요청 글 수 결정 (1 ~ 10개)
      int postRemaining = postCount - totalCreated;
      int numPosts = random.nextInt(10) + 1; // 1 ~ 10
      numPosts = Math.min(numPosts, postRemaining); // 남은 수보다 많지 않도록 조정

      // 자료 요청 글 생성
      for (int i = 0; i < numPosts; i++) {
        DocumentRequestPost documentRequestPost = testDataGenerator.createMockDocumentRequestPost(postWriter);
        documentRequestPostRepository.save(documentRequestPost);
        totalCreated++;

        // 댓글 작성
        int numComments = random.nextInt(6); // 0 ~ 5
        for (int j = 0; j < numComments; j++) {
          // 회원 풀에서 랜덤으로 댓글 작성자 선택
          Member commentWriter = memberPool.get(random.nextInt(memberPool.size()));
          Comment comment = testDataGenerator.createMockComment(
              commentWriter,
              documentRequestPost.getDocumentRequestPostId(),
              ContentType.DOCUMENT_REQUEST
          );
          documentRequestPost.increaseCommentCount();
        }
      }
    }

    log.info("총 {} 명의 mock 유저가 {} 개의 자료 요청 글을 생성했습니다.",
        userCount, totalCreated);
  }

  // 회원 풀 생성
  private List<Member> createMemberPool(Integer postCount) {
    int memberPoolSize = Math.min(50, Math.max(10, postCount / 10));
    List<Member> memberPool = new ArrayList<>();
    for (int i = 0; i < memberPoolSize; i++) {
      Member member = testDataGenerator.createMockMember();
      memberPool.add(member);
    }
    return memberPool;
  }

  /**
   * FTP 서버에서 파일을 다운로드하여 바이트 배열로 반환합니다.
   *
   * @param filePath FTP 서버 상의 파일 경로
   * @return 파일의 바이트 배열
   */
  @Transactional(readOnly = true)
  public byte[] downloadFile(String filePath) {
    FTPClient ftpClient = null;
    try {
      // FTP 클라이언트 풀에서 클라이언트 가져오기
      ftpClient = ftpClientPool.borrowObject();

      // FTP 서버에서 파일 스트림 가져오기
      InputStream inputStream = ftpClient.retrieveFileStream(filePath);
      if (inputStream == null) {
        throw new CustomException(ErrorCode.FILE_NOT_FOUND);
      }

      // InputStream을 바이트 배열로 변환
      byte[] fileBytes = IOUtils.toByteArray(inputStream);
      inputStream.close();

      // FTP 명령 완료 확인
      boolean success = ftpClient.completePendingCommand();
      if (!success) {
        throw new CustomException(ErrorCode.FTP_DOWNLOAD_ERROR);
      }

      return fileBytes;
    } catch (Exception e) {
      log.error("파일 다운로드 중 오류 발생: {}", e.getMessage());
      throw new CustomException(ErrorCode.FTP_DOWNLOAD_ERROR);
    } finally {
      if (ftpClient != null) {
        try {
          // FTP 클라이언트를 풀로 반환
          ftpClientPool.returnObject(ftpClient);
        } catch (Exception e) {
          log.error("FTP 클라이언트를 풀에 반환하는 중 오류 발생: {}", e.getMessage());
        }
      }
    }
  }

  /**
   * 네이버 지시인 스크래핑 -> QuestionPost 생성
   */
  @Transactional
  public TestDto createMockQuestionPostFromKinNaver(TestCommand command) {
    long startTime = System.currentTimeMillis();

    Integer postCount = command.getPostCount();

    // 유효성 검사 및 기본값 설정
    if (postCount == null || postCount <= 0) {
      log.warn("잘못된 작성 개수가 입력되었습니다. 기본 값 30개로 설정합니다.");
      postCount = 30;
    }

    // 작성자 결정 (useMockMember 값에 따라 실제 Member 또는 가짜 Member 선택)
    Member member;
    if (command.isUseMockMember()) {
      member = testDataGenerator.createMockMember(); // 가짜 Member
      log.info("ID가 {}인 mock 회원을 생성했습니다.", member.getMemberId());
    } else {
      member = command.getMember(); // 실제 Member
    }

    int collectedPosts = 0;
    boolean hasNextPage = true;

    // 초기 페이지 로드
    String url = "https://kin.naver.com/index.naver";
    WebDriver driver = webDriverManager.getDriver();
    WebDriverWait wait = webDriverManager.getWait();
    driver.get(url);
    try {
      wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".answer_box._noanswerItem")));
    } catch (TimeoutException te) {
      log.error("초기 페이지 로딩에 실패했습니다.", te);
      return TestDto.builder()
          .createdPostCount(0)
          .timeTaken(TimeUtil.convertMillisToReadableTime(System.currentTimeMillis() - startTime))
          .build();
    }

    while (collectedPosts < postCount && hasNextPage) {
      log.info("현재 페이지 스크래핑 중");

      Document doc = Jsoup.parse(driver.getPageSource());
      Elements answerBoxes = doc.select(".answer_box._noanswerItem");

      log.info("현재 페이지에서 {}개의 답변 상자를 찾았습니다.", answerBoxes.size());

      if (answerBoxes.isEmpty()) {
        log.warn("현재 페이지에서 답변 상자를 찾을 수 없어 스크래핑을 중단합니다.");
        break;
      }

      for (Element answerBox : answerBoxes) {
        if (collectedPosts >= postCount) {
          break;
        }

        // docId 추출
        Element linkElement = answerBox.selectFirst("a._first_focusable_link");
        if (linkElement == null) {
          log.warn("링크 요소를 찾을 수 없어 게시물을 건너뜁니다.");
          continue;
        }

        String href = linkElement.attr("href");
        String docId = getDocIdFromHref(href);
        if (docId == null || processedDocIds.contains(docId)) {
          log.info("중복되거나 유효하지 않은 docId: {}을(를) 건너뜁니다.", docId);
          continue;
        }

        processedDocIds.add(docId);

        Element titleElement = answerBox.selectFirst(".tit_wrap .tit_txt");
        Element contentElement = answerBox.selectFirst(".tit_wrap .txt");

        if (titleElement == null) {
          log.warn("제목 요소를 찾을 수 없어 게시물을 건너뜁니다.");
          continue;
        }

        String title = titleElement.text();
        String content = contentElement != null ? contentElement.text() : "";

        log.info("게시물 처리 중: {}", title);

        // 태그 처리
        List<String> customTags = null;
        Element tagList = answerBox.selectFirst(".tagList");
        if (tagList != null && !tagList.hasAttr("style")) {
          Elements tagElements = tagList.select(".tag");
          if (!tagElements.isEmpty()) {
            customTags = new ArrayList<>();
            for (Element tagElement : tagElements) {
              String tag = tagElement.text().replace("#", "").trim();
              if (!tag.isEmpty() && tag.length() <= 10) {
                customTags.add(tag);
                if (customTags.size() >= 4) { // MAX_CUSTOM_TAGS=4
                  break;
                }
              }
            }
            if (customTags.isEmpty()) {
              customTags = null;
            }
          }
        }

        Element dirLink = answerBox.selectFirst(".update_info .info a");
        String dirName = dirLink != null ? dirLink.text() : "";

        QuestionCommand postCommand = QuestionCommand.builder()
            .memberId(member.getMemberId())
            .title(title)
            .content(content)
            .subject(getValidSubjectOrRandom(dirName))
            .rewardYeopjeon(100)
            .isPrivate(false)
            .customTags(customTags)
            .build();

        try {
          questionPostService.saveQuestionPost(postCommand);
          log.info("게시물 생성 성공: {} 태그: {}", title, customTags);
          collectedPosts++;
        } catch (Exception e) {
          log.error("게시물 저장 실패: {}", title, e);
        }
      }

      // 페이지 네비게이션 로깅
      logPaginationElements();

      // "다음" 버튼 클릭 시도
      try {
        WebElement nextButton = wait.until(
            ExpectedConditions.elementToBeClickable(By.cssSelector("#pagingArea0 a.next")));
        if (nextButton != null && nextButton.isDisplayed()) {
          log.info("다음 페이지로 이동하기 위해 '다음' 버튼 클릭");
          nextButton.click();

          // 새로운 페이지가 로드될 때까지 대기
          wait.until(ExpectedConditions.stalenessOf(nextButton));
          wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".answer_box._noanswerItem")));
        } else {
          log.info("'다음' 버튼을 찾을 수 없습니다. 마지막 페이지에 도달했습니다.");
          hasNextPage = false;
        }
      } catch (TimeoutException te) {
        log.warn("시간 내에 '다음' 버튼을 찾을 수 없어 마지막 페이지로 간주합니다.");
        hasNextPage = false;
      } catch (Exception e) {
        log.error("'다음' 버튼을 찾거나 클릭하는 데 실패했습니다. 페이지네이션을 중단합니다.", e);
        hasNextPage = false;
      }
    }

    long endTime = System.currentTimeMillis();
    long durationMillis = endTime - startTime;
    String readableTime = TimeUtil.convertMillisToReadableTime(durationMillis);

    log.info("{}개의 mock 게시물을 {} 안에 성공적으로 생성했습니다.", collectedPosts, readableTime);

    return TestDto.builder()
        .createdPostCount(collectedPosts)
        .timeTaken(readableTime)
        .build();
  }

  /**
   * href에서 docId 추출
   *
   * @param href href 문자열
   * @return docId 또는 null
   */
  private String getDocIdFromHref(String href) {
    // href 예시: /qna/detail.naver?d1id=8&dirId=814&docId=479952838
    try {
      String[] parts = href.split("&");
      for (String part : parts) {
        if (part.startsWith("docId=")) {
          return part.substring(6);
        }
      }
    } catch (Exception e) {
      log.error("href에서 docId를 추출하는 데 실패했습니다: {}", href, e);
    }
    return null;
  }

  /**
   * 현재 페이지의 페이지 네비게이션 요소 로깅
   */
  private void logPaginationElements() {
    WebDriver driver = webDriverManager.getDriver();
    Document doc = Jsoup.parse(driver.getPageSource());
    Element pagingArea = doc.selectFirst("#pagingArea0");
    if (pagingArea != null) {
      Elements prevButton = pagingArea.select("a.prev");
      Elements nextButton = pagingArea.select("a.next");
      Elements pageNumbers = pagingArea.select("a.number");

      log.info("이전 버튼 존재 여부: {}", !prevButton.isEmpty());
      log.info("다음 버튼 존재 여부: {}", !nextButton.isEmpty());

      log.info("페이지 번호:");
      for (Element page : pageNumbers) {
        log.info(" - 페이지 번호: {}, 클래스: {}", page.text(), page.className());
      }
    } else {
      log.warn("페이지 네비게이션 영역을 찾을 수 없습니다.");
    }
  }

  /**
   * 유효한 subject 반환 또는 랜덤 선택
   *
   * @param dirName 카테고리 이름
   * @return subject 이름
   */
  private String getValidSubjectOrRandom(String dirName) {
    return subjectRepository.findAll().stream()
        .filter(s -> s.getName().equalsIgnoreCase(dirName))
        .findFirst()
        .map(Subject::getName)
        .orElseGet(() -> {
          List<String> subjects = subjectRepository.findAll().stream()
              .map(Subject::getName)
              .toList();
          return subjects.get(random.nextInt(subjects.size()));
        });
  }
}
