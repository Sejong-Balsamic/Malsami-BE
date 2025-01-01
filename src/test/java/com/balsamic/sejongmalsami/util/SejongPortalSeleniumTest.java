package com.balsamic.sejongmalsami.util;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
public class SejongPortalSeleniumTest {

  @Value("${sejong.portal.id}")
  private String sejongPortalId;

  @Value("${sejong.portal.password}")
  private String sejongPortalPassword;

  @Test
  public void mainTest() throws Exception {
    testSejongPortalLogin();
  }

  public void testSejongPortalLogin() throws InterruptedException {
    WebDriverManager.chromedriver().setup();
    WebDriver driver = new ChromeDriver();
    driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));

    try {
      // 포털 로그인 페이지로 이동
      driver.get("https://portal.sejong.ac.kr/jsp/login/loginSSL.jsp");

      try {
        WebElement chkNos = driver.findElement(By.id("chkNos"));
        if (!chkNos.isSelected()) {
          chkNos.click();
          // 팝업(Alert) 뜨면 수동으로 수락:
          Thread.sleep(1000);
          try {
            Alert alert = driver.switchTo().alert();
            alert.accept();
          } catch (NoAlertPresentException e) {
            // 팝업이 없으면 무시
          }
        }
      } catch (NoSuchElementException e) {
        System.out.println("키보드보안 체크박스를 찾지 못했습니다. 넘어갑니다.");
      }

      // 아이디/비밀번호 입력
      WebElement userId = driver.findElement(By.id("id"));
      WebElement userPw = driver.findElement(By.id("password"));
      userId.sendKeys(sejongPortalId);
      userPw.sendKeys(sejongPortalPassword);

      // 로그인 버튼 클릭
      WebElement loginBtn = driver.findElement(By.id("loginBtn"));
      loginBtn.click();

      // 대기
      Thread.sleep(3000);

      // 로그인 성공 여부 검사
      String currentUrl = driver.getCurrentUrl();
      System.out.println("현재 URL: " + currentUrl);

      if (currentUrl.contains("login_noaccess.jsp")) {
        System.out.println("서버가 noaccess.jsp를 로딩 - 계정 거부 상태");
      } else {
        System.out.println("로그인 성공(추정). 현재 URL = " + currentUrl);
      }

      // 고전독서인증 페이지 이동
      driver.get("https://classic.sejong.ac.kr/classic/reading/status.do");
      Thread.sleep(3000);
      System.out.println("고전독서인증현황 URL -> " + driver.getCurrentUrl());

      // 쿠키 로깅
      for (Cookie c : driver.manage().getCookies()) {
        System.out.println("[COOKIE] " + c);
      }

      // HTML 파싱
      String pageSource = driver.getPageSource();
      Document document = Jsoup.parse(pageSource);
      parseAndLogUserInfo(document);

    } finally {
      // 브라우저 닫기
      Thread.sleep(2000);
      driver.quit();
    }
  }

  private void parseAndLogUserInfo(Document doc) {
    String selector = ".b-con-box:has(h4.b-h4-tit01:contains(사용자 정보)) table.b-board-table tbody tr";
    List<String> rowLabels = new ArrayList<>();
    List<String> rowValues = new ArrayList<>();

    doc.select(selector).forEach(row -> {
      String label = row.select("th").text().trim();
      String value = row.select("td").text().trim();
      rowLabels.add(label);
      rowValues.add(value);
    });

    String major = null;
    String studentId = null;
    String studentName = null;
    String academicYear = null;
    String enrollmentStatus = null;

    for (int i = 0; i < rowLabels.size(); i++) {
      String label = rowLabels.get(i);
      String value = rowValues.get(i);

      switch (label) {
        case "학과명":
          major = value;
          break;
        case "학번":
          studentId = value;
          break;
        case "이름":
          studentName = value;
          break;
        case "학년":
          academicYear = value;
          break;
        case "사용자 상태":
          enrollmentStatus = value;
          break;
        default:
          break;
      }
    }

    LogUtil.lineLog(null);
    log.info("사용자 정보 파싱 결과");
    log.info("학과명(major)       : {}", major);
    log.info("학번(studentId)     : {}", studentId);
    log.info("이름(studentName)   : {}", studentName);
    log.info("학년(academicYear)  : {}", academicYear);
    log.info("사용자상태(enrollSt): {}", enrollmentStatus);
    LogUtil.lineLog(null);
  }
}
