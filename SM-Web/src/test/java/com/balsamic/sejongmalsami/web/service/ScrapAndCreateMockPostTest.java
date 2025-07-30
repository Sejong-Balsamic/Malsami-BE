package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.application.test.TestDataGenerator;
import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.postgres.Member;
import com.balsamic.sejongmalsami.repository.postgres.SubjectRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
public class ScrapAndCreateMockPostTest {

  private static final int CREATE_QUESTION_POST_COUNT = 50;
  private static final int MAX_CUSTOM_TAGS = 4;
  private static WebDriver driver;
  private static WebDriverWait wait;
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  private QuestionPostService questionPostService;

  @Autowired
  private SubjectRepository subjectRepository;

  @Autowired
  private TestDataGenerator testDataGenerator;

  private Set<String> processedDocIds = new HashSet<>();

  @BeforeAll
  public static void setUp() {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless");
    options.addArguments("--disable-gpu");
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");

    driver = new ChromeDriver(options);
    wait = new WebDriverWait(driver, Duration.ofSeconds(10));
  }

  @AfterAll
  public static void tearDown() {
    if (driver != null) {
      driver.quit();
    }
  }

  @Test
  public void mainTest() throws IOException, InterruptedException {
    scrapNaverKinAndCreateQuestionPosts();
  }

  private void scrapNaverKinAndCreateQuestionPosts() throws IOException, InterruptedException {
    Member member = testDataGenerator.createMockMember();
    log.info("Created mock member with ID: {}", member.getMemberId());

    int collectedPosts = 0;
    boolean hasNextPage = true;

    // 초기 페이지 로드
    String url = "https://kin.naver.com/index.naver";
    driver.get(url);
    wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".answer_box._noanswerItem")));

    while (collectedPosts < CREATE_QUESTION_POST_COUNT && hasNextPage) {
      log.info("Scraping current page");

      Document doc = Jsoup.parse(driver.getPageSource());
      Elements answerBoxes = doc.select(".answer_box._noanswerItem");

      log.info("Found {} answer boxes on current page", answerBoxes.size());

      if (answerBoxes.isEmpty()) {
        log.warn("No answer boxes found on current page, stopping scraping");
        break;
      }

      for (Element answerBox : answerBoxes) {
        if (collectedPosts >= CREATE_QUESTION_POST_COUNT) break;

        // docId 추출
        Element linkElement = answerBox.selectFirst("a._first_focusable_link");
        if (linkElement == null) {
          log.warn("Link element not found, skipping post");
          continue;
        }

        String href = linkElement.attr("href");
        String docId = getDocIdFromHref(href);
        if (docId == null || processedDocIds.contains(docId)) {
          log.info("Duplicate or invalid docId: {}, skipping", docId);
          continue;
        }

        processedDocIds.add(docId);

        Element titleElement = answerBox.selectFirst(".tit_wrap .tit_txt");
        Element contentElement = answerBox.selectFirst(".tit_wrap .txt");

        if (titleElement == null) {
          log.warn("Title element not found, skipping post");
          continue;
        }

        String title = titleElement.text();
        String content = contentElement != null ? contentElement.text() : "";

        log.info("Processing post: {}", title);

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
                if (customTags.size() >= MAX_CUSTOM_TAGS) {
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

        QuestionCommand command = QuestionCommand.builder()
            .memberId(member.getMemberId())
            .title(title)
            .content(content)
            .subject(getValidSubjectOrRandom(dirName))
            .rewardYeopjeon(100)
            .isPrivate(false)
            .customTags(customTags)
            .build();

        try {
          QuestionDto savedPost = questionPostService.saveQuestionPost(command);
          log.info("Successfully created post: {} with tags: {}", savedPost.getQuestionPost().getTitle(), customTags);
          collectedPosts++;
        } catch (Exception e) {
          log.error("Failed to save post: {}", title, e);
        }
      }

      // 페이지 네비게이션 로깅
      logPaginationElements();

      // "다음" 버튼 클릭 시도
      try {
        WebElement nextButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#pagingArea0 a.next")));
        if (nextButton != null && nextButton.isDisplayed()) {
          log.info("Clicking '다음' button to go to the next page");
          nextButton.click();

          // 새로운 페이지가 로드될 때까지 대기
          wait.until(ExpectedConditions.stalenessOf(nextButton));
          wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".answer_box._noanswerItem")));
        } else {
          log.info("No '다음' button found. Reached the last page.");
          hasNextPage = false;
        }
      } catch (TimeoutException te) {
        log.warn("No '다음' button found within timeout. Assuming last page.");
        hasNextPage = false;
      } catch (Exception e) {
        log.error("Failed to find or click '다음' button. Stopping pagination.", e);
        hasNextPage = false;
      }
    }

    log.info("Successfully created {} mock posts", collectedPosts);
  }

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
      log.error("Failed to extract docId from href: {}", href, e);
    }
    return null;
  }

  private void logPaginationElements() {
    Document doc = Jsoup.parse(driver.getPageSource());
    Element pagingArea = doc.selectFirst("#pagingArea0");
    if (pagingArea != null) {
      Elements prevButton = pagingArea.select("a.prev");
      Elements nextButton = pagingArea.select("a.next");
      Elements pageNumbers = pagingArea.select("a.number");

      log.info("Prev Button Present: {}", !prevButton.isEmpty());
      log.info("Next Button Present: {}", !nextButton.isEmpty());

      log.info("Page Numbers:");
      for (Element page : pageNumbers) {
        log.info(" - Page Number: {}, Class: {}", page.text(), page.className());
      }
    } else {
      log.warn("Paging area not found.");
    }
  }

  private String getCookies() {
    StringBuilder cookieString = new StringBuilder();
    Set<Cookie> cookies = driver.manage().getCookies();
    for (Cookie cookie : cookies) {
      cookieString.append(cookie.getName()).append("=").append(cookie.getValue()).append("; ");
    }
    return cookieString.toString();
  }

  private String getValidSubjectOrRandom(String dirName) {
    return subjectRepository.findAll().stream()
        .filter(s -> s.getName().equalsIgnoreCase(dirName))
        .findFirst()
        .map(s -> s.getName())
        .orElseGet(() -> {
          List<String> subjects = subjectRepository.findAll().stream()
              .map(s -> s.getName())
              .toList();
          return subjects.get(new Random().nextInt(subjects.size()));
        });
  }
}
