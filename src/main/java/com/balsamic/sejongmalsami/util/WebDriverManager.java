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
  private final ChromeOptions options;
  private final String seleniumGridUrl;
  private final boolean isServerEnvironment;

  // ThreadLocal -> 각 스레드 WebDriver 인스턴스 관리
  private final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
  private final ThreadLocal<WebDriverWait> waitThreadLocal = new ThreadLocal<>();

  public WebDriverManager(@Value("${selenium.grid-url}") String seleniumGridUrl) {
    this.seleniumGridUrl = seleniumGridUrl;
    this.isServerEnvironment = FileUtil.getCurrentSystem().equals(SystemType.LINUX);
    this.options = new ChromeOptions();
    this.options.addArguments("--headless", "--disable-gpu", "--no-sandbox", "--disable-dev-shm-usage");
  }

  /**
   * WebDriver 가져오기 (필요 시 초기화)
   */
  public WebDriver getDriver() {
    WebDriver driver = driverThreadLocal.get();
    if (driver == null || ((RemoteWebDriver) driver).getSessionId() == null) {
      driver = initDriver();
      driverThreadLocal.set(driver);
    }
    return driver;
  }

  /**
   * WebDriver 초기화
   */
  private WebDriver initDriver() {
    try {
      WebDriver driver;
      if (isServerEnvironment) {
        // 서버 환경에서는 Selenium Grid에 연결
        driver = new RemoteWebDriver(new URL(seleniumGridUrl), options);
        LogUtil.lineLog("Selenium Grid에 연결된 WebDriver 초기화 완료");
      } else {
        // 로컬 개발 환경에서는 ChromeDriver 사용
        driver = new ChromeDriver(options);
        LogUtil.lineLog("로컬 WebDriver 초기화 완료");
      }
      WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
      waitThreadLocal.set(wait);
      return driver;
    } catch (MalformedURLException e) {
      throw new RuntimeException("Selenium Grid URL이 잘못되었습니다.", e);
    }
  }

  /**
   * WebDriver 종료 및 메모리 정리
   */
  public void quitDriver() {
    WebDriver driver = driverThreadLocal.get();
    if (driver != null) {
      driver.quit();
      driverThreadLocal.remove();
      waitThreadLocal.remove();
      LogUtil.lineLog("WebDriver 세션 종료");
    }
  }

  /**
   * WebDriverWait 가져오기
   */
  public WebDriverWait getWait() {
    WebDriverWait wait = waitThreadLocal.get();
    if (wait == null) {
      getDriver(); // driver가 없으면 초기화
      wait = waitThreadLocal.get();
    }
    return wait;
  }

  /**
   * 현재 WebDriver 세션 ID 가져오기
   */
  public SessionId getSessionId() {
    WebDriver driver = driverThreadLocal.get();
    if (driver != null) {
      return ((RemoteWebDriver) driver).getSessionId();
    }
    return null;
  }
}
