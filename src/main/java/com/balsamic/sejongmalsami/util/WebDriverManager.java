package com.balsamic.sejongmalsami.util;

import com.balsamic.sejongmalsami.object.constants.SystemType;
import com.balsamic.sejongmalsami.util.log.LogUtil;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WebDriverManager {
  private WebDriver driver;
  private WebDriverWait wait;
  private final ChromeOptions options;
  private final String seleniumGridUrl;
  private final boolean isServerEnvironment;


  public WebDriverManager(@Value("${selenium.grid-url}") String seleniumGridUrl) {
    this.seleniumGridUrl = seleniumGridUrl;
    this.isServerEnvironment = FileUtil.getCurrentSystem().equals(SystemType.LINUX);
    this.options = new ChromeOptions();
    this.options.addArguments("--headless", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage");
  }

  /**
   * WebDriver 가져오기 (필요 시 초기화)
   */
  public synchronized WebDriver getDriver() {
    if (driver == null || ((RemoteWebDriver) driver).getSessionId() == null) {
      initDriver();
    }
    return driver;
  }

  /**
   * WebDriver 초기화
   */
  private void initDriver() {
    try {
      if (isServerEnvironment) {
        // 서버 환경에서는 Selenium Grid에 연결
        driver = new RemoteWebDriver(new URL(seleniumGridUrl), options);
        LogUtil.lineLog("Selenium Grid에 연결된 WebDriver 초기화 완료");
      } else {
        // 로컬 개발 환경에서는 ChromeDriver 사용
        driver = new ChromeDriver(options);
        LogUtil.lineLog("로컬 WebDriver 초기화 완료");
      }
      wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    } catch (MalformedURLException e) {
      throw new RuntimeException("Selenium Grid URL이 잘못되었습니다.", e);
    }
  }

  /**
   * WebDriver 종료 및 메모리 정리
   */
  public synchronized void quitDriver() {
    if (driver != null) {
      driver.quit();
      driver = null; // 메모리에서 제거
      LogUtil.lineLog("WebDriver 세션 종료");
    }
  }

  /**
   * WebDriverWait 가져오기
   */
  public WebDriverWait getWait() {
    getDriver(); // driver가 없으면 초기화
    return wait;
  }

  /**
   * 현재 WebDriver 세션 ID 가져오기
   */
  public synchronized SessionId getSessionId() {
    if (driver != null) {
      return ((RemoteWebDriver) driver).getSessionId();
    }
    return null;
  }
}
