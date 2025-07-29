package com.balsamic.sejongmalsami.util;

import static com.balsamic.sejongmalsami.util.log.LogUtil.lineLog;
import static com.balsamic.sejongmalsami.util.log.LogUtil.timeLog;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.SocketTimeoutException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
public class SejongPortalAuthenticatorTest {

  @Value("${sejong.portal.id}")
  private String sejongPortalId;

  @Value("${sejong.portal.password}")
  private String sejongPortalPassword;

  @Test
  public void mainTest() throws Exception {
    timeLog(this::testSejongPortalLogin);
  }

  public void testSejongPortalLogin() throws Exception {
    String id = sejongPortalId;
    String password = sejongPortalPassword;

    String loginUrl = "https://portal.sejong.ac.kr/jsp/login/login_action.jsp";
    String redirectUrl = "http://classic.sejong.ac.kr/_custom/sejong/sso/sso-return.jsp?returnUrl=https://classic.sejong.ac.kr/classic/index.do";
    String finalUrl = "https://classic.sejong.ac.kr/classic/reading/status.do";

    OkHttpClient client = buildClient();

    // 1) 로그인 요청
    FormBody formData = new FormBody.Builder()
        .add("mainLogin", "N")
        .add("rtUrl", "library.sejong.ac.kr")
        .add("id", id)
        .add("password", password)
        .build();

    Request loginRequest = new Request.Builder()
        .url(loginUrl)
        .post(formData)
        .header("Host", "portal.sejong.ac.kr")
        .header("Referer", "https://portal.sejong.ac.kr")
        .header("Cookie", "chknos=false")
        .build();

    Response loginResponse = null;
    while (true) {
      try {
        loginResponse = client.newCall(loginRequest).execute();
        break;
      } catch (SocketTimeoutException e) {
        log.warn("Timeout 발생 -> 재시도...");
      } catch (IOException e) {
        log.error("로그인 요청 중 오류 발생: {}", e.getMessage());
        return;
      }
    }

    if (loginResponse == null || loginResponse.body() == null) {
      log.error("로그인 요청 실패: 재시도 모두 실패");
      return;
    }
    log.info("로그인 요청 완료. 응답 코드: {}", loginResponse.code());

    loginResponse.close();

    // 2) SSO 리다이렉트 요청
    Request redirectRequest = new Request.Builder().url(redirectUrl).get().build();
    try (Response redirectResponse = client.newCall(redirectRequest).execute()) {
      log.info("SSO 리다이렉트 요청 완료. 응답 코드: {}", redirectResponse.code());
    }

    // 3) 고전독서인증현황 페이지(GET)
    Request finalRequest = new Request.Builder().url(finalUrl).get().build();
    String finalHtml;
    try (Response finalResponse = client.newCall(finalRequest).execute()) {
      if (finalResponse.body() == null) {
        log.error("최종 페이지 응답 바디가 없습니다.");
        return;
      }
      finalHtml = finalResponse.body().string();
      log.info("최종 페이지 요청 완료. 응답 코드: {}", finalResponse.code());
    }

    // 4) Jsoup으로 파싱하여 필요한 정보 추출
    parseAndLogUserInfo(finalHtml);
  }

  private void parseAndLogUserInfo(String html) {
    Document doc = Jsoup.parse(html);

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

    lineLog("사용자 정보 파싱 결과");
    log.info("학과명(major)       : {}", major);
    log.info("학번(studentId)     : {}", studentId);
    log.info("이름(studentName)   : {}", studentName);
    log.info("학년(academicYear)  : {}", academicYear);
    log.info("사용자상태(enrollSt): {}", enrollmentStatus);

    parseAndLogAllInfos(html);
  }

  private OkHttpClient buildClient() throws Exception {
    SSLContext sslContext = SSLContext.getInstance("SSL");
    sslContext.init(null, new TrustManager[]{trustAllManager()}, new java.security.SecureRandom());
    SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

    HostnameVerifier hostnameVerifier = (hostname, session) -> true;

    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(log::info);
    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

    CookieManager cookieManager = new CookieManager();
    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    JavaNetCookieJar cookieJar = new JavaNetCookieJar(cookieManager);

    return new OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .hostnameVerifier(hostnameVerifier)
        .sslSocketFactory(sslSocketFactory, trustAllManager())
        .readTimeout(500, TimeUnit.MILLISECONDS)
        .addInterceptor(loggingInterceptor)
        .build();
  }

  private X509TrustManager trustAllManager() {
    return new X509TrustManager() {
      @Override
      public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}

      @Override
      public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}

      @Override
      public java.security.cert.X509Certificate[] getAcceptedIssuers() {
        return new java.security.cert.X509Certificate[0];
      }
    };
  }

  private void parseAndLogAllInfos(String html) {
    Document doc = Jsoup.parse(html);

    lineLog("사용자 정보");
    String selector = ".b-con-box:has(h4.b-h4-tit01:contains(사용자 정보)) table.b-board-table tbody tr";
    doc.select(selector).forEach(tr -> {
      String label = tr.select("th").text().trim();
      String value = tr.select("td").text().trim();
      log.info("{} = {}", label, value);
    });

    lineLog("영역별 인증현황");
    selector = ".b-con-box:has(h4.b-h4-tit01:contains(영역별 인증현황)) table.b-board-table tbody tr";
    doc.select(selector).forEach(tr -> {
      String col1 = tr.select("th").text().trim();
      String col2 = tr.select("td").first() != null ? tr.select("td").first().text().trim() : "";
      String col3 = tr.select("td").last() != null ? tr.select("td").last().text().trim() : "";
      log.info("{} | 이수권수: {} | 인증권수: {}", col1, col2, col3);
    });

    lineLog("인증 시험 현황");
    selector = ".b-con-box:has(h4.b-h4-tit01:contains(인증 시험 현황)) table.b-board-table tbody tr";
    doc.select(selector).forEach(tr -> {
      List<String> cols = tr.select("td").eachText();
      if (cols.size() < 6) {
        log.info(tr.text());
      } else {
        log.info("년도/학기={}; 영역명={}; 도서명={}; 응시일자={}; 점수={}; 합격여부={}",
            cols.get(0), cols.get(1), cols.get(2), cols.get(3), cols.get(4), cols.get(5));
      }
    });

    lineLog("과목 대체 인증 현황");
    selector = ".b-con-box:has(h4.b-h4-tit01:contains(과목 대체 인증 현황)) table.b-board-table tbody tr";
    doc.select(selector).forEach(tr -> {
      List<String> cols = tr.select("td").eachText();
      if (cols.size() < 5) {
        log.info(tr.text());
      } else {
        log.info("년도/학기={}; 과목명={}; 영역명={}; 도서명={}; 이수여부={}",
            cols.get(0), cols.get(1), cols.get(2), cols.get(3), cols.get(4));
      }
    });

    lineLog("대회 인증 현황");
    selector = ".b-con-box:has(h4.b-h4-tit01:contains(대회 인증 현황)) table.b-board-table tbody tr";
    doc.select(selector).forEach(tr -> {
      List<String> cols = tr.select("td").eachText();
      if (cols.size() < 4) {
        log.info(tr.text());
      } else {
        log.info("년도/학기={}; 대회명={}; 영역명={}; 도서명={}",
            cols.get(0), cols.get(1), cols.get(2), cols.get(3));
      }
    });

    lineLog("교과연계 인증 현황");
    selector = ".b-con-box:has(h4.b-h4-tit01:contains(교과연계 인증 현황)) table.b-board-table tbody tr";
    doc.select(selector).forEach(tr -> {
      List<String> cols = tr.select("td").eachText();
      if (cols.size() < 5) {
        log.info(tr.text());
      } else {
        log.info("년도/학기={}; 과목명={}; 영역명={}; 도서명={}; 이수구분={}",
            cols.get(0), cols.get(1), cols.get(2), cols.get(3), cols.get(4));
      }
    });

    lineLog("모든 정보 로그 출력 완료");
  }
}
