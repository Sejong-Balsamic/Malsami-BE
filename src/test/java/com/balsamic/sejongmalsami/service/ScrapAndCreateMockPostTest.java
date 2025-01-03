package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.repository.postgres.SubjectRepository;
import com.balsamic.sejongmalsami.util.TestDataGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
public class ScrapAndCreateMockPostTest {

  private static final int CREATE_QUESTION_POST_COUNT = 5;
  private static final int MAX_CUSTOM_TAGS = 4;
  private static WebDriver driver;
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Autowired
  private QuestionPostService questionPostService;

  @Autowired
  private SubjectRepository subjectRepository;

  @Autowired
  private TestDataGenerator testDataGenerator;

  @BeforeAll
  public static void setUp() {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless");
    options.addArguments("--disable-gpu");
    options.addArguments("--no-sandbox");
    options.addArguments("--disable-dev-shm-usage");

    driver = new ChromeDriver(options);
  }

  @AfterAll
  public static void tearDown() {
    if (driver != null) {
      driver.quit();
    }
  }

  @Test
  public void mainTest() throws IOException, InterruptedException {
    scrapNaverKinAndCreatePosts();
  }

  private void scrapNaverKinAndCreatePosts() throws IOException, InterruptedException {
    // 1. 먼저 Member 생성하고 저장
    Member member = testDataGenerator.createMockMember();
    log.info("Created mock member with ID: {}", member.getMemberId());

    int collectedPosts = 0;
    int currentPage = 1;

    while (collectedPosts < CREATE_QUESTION_POST_COUNT) {
      // 2. 페이징된 URL로 요청
      String queryTime = LocalDateTime.now()
          .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
      String encodedQueryTime = URLEncoder.encode(queryTime, StandardCharsets.UTF_8);

      String apiUrl = String.format(
          "https://kin.naver.com/ajax/mainNoanswer.naver?page=%d&dirId=0&selTab=qna&queryTime=%s&countPerPage=20&viewType=preview",
          currentPage,
          encodedQueryTime
      );

      HttpClient client = HttpClient.newBuilder()
          .version(HttpClient.Version.HTTP_1_1)
          .build();

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(apiUrl))
          .header("Cookie", getCookies())
          .header("Referer", "https://kin.naver.com/")
          .header("User-Agent", "Mozilla/5.0")
          .GET()
          .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      JsonNode rootNode = objectMapper.readTree(response.body());

      JsonNode listNode = rootNode.path("result").get(0).path("noanswer").path("list");
      for (JsonNode item : listNode) {
        if (collectedPosts >= CREATE_QUESTION_POST_COUNT) break;

        String title = item.path("title").asText();
        String content = item.path("previewContents").asText();
        String dirName = item.path("dirName").asText();

        // 질문 상세 페이지 URL을 구성하여 접근
        String questionUrl = "https://kin.naver.com" + item.path("questionUrl").asText();
        driver.get(questionUrl);

        // 페이지 로딩을 기다림
        Thread.sleep(1000);

        // 이제 페이지 소스를 파싱
        String pageSource = driver.getPageSource();
        Document doc = Jsoup.parse(pageSource);
        Elements tagElements = doc.select(".tagList .tag");

        List<String> customTags = new ArrayList<>();
        for (Element tagElement : tagElements) {
          String tag = tagElement.text().replace("#", "").trim(); // # 제거하고 공백 제거
          if (!tag.isEmpty()) {
            customTags.add(tag);
          }
          if (customTags.size() >= MAX_CUSTOM_TAGS) {
            break;
          }
        }

        QuestionCommand command = QuestionCommand.builder()
            .memberId(member.getMemberId())
            .title(title)
            .content(content)
            .subject(getValidSubjectOrRandom(dirName))
            .rewardYeopjeon(100)
            .isPrivate(false)
            .customTags(customTags.isEmpty() ? null : customTags)
            .build();

        try {
          QuestionDto savedPost = questionPostService.saveQuestionPost(command);
          log.info("Created post: {} with tags: {}", savedPost.getQuestionPost().getTitle(), customTags);
          collectedPosts++;
        } catch (Exception e) {
          log.error("Failed to save post: {}", title, e);
        }
      }

      currentPage++;
      Thread.sleep(1000); // 약간의 지연을 주어 서버 부하 방지
    }

    log.info("Successfully created {} mock posts", collectedPosts);
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