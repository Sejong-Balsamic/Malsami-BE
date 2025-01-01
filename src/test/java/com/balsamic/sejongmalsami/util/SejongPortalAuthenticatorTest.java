package com.balsamic.sejongmalsami.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
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
    testSejongPortalLogin();
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
        // 쿠키
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

    // 응답 본문 혹은 쿠키 등 추가 확인이 필요하다면 여기에 로직 추가
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

    // (선택) HTML 내용을 파일로 저장해 확인
    try (FileOutputStream fos = new FileOutputStream("test.html")) {
      fos.write(finalHtml.getBytes(StandardCharsets.UTF_8));
    }
    log.info("test.html 저장 완료.");

    // 4) Jsoup으로 파싱하여 필요한 정보 추출
    parseAndLogUserInfo(finalHtml);
//    parseAndLogAllInfos(finalHtml);
  }

  /**
   * 최종 HTML에서 "사용자 정보" 테이블을 Jsoup으로 파싱해 학과명/학번/이름/학년/사용자상태 를 추출/로그.
   */
  private void parseAndLogUserInfo(String html) {
    // 1) Jsoup parse
    Document doc = Jsoup.parse(html);

    // 2) "사용자 정보" 테이블 행들 추출
    String selector =
        ".b-con-box:has(h4.b-h4-tit01:contains(사용자 정보)) table.b-board-table tbody tr";
    List<String> rowLabels = new ArrayList<>();
    List<String> rowValues = new ArrayList<>();

    doc.select(selector).forEach(row -> {
      String label = row.select("th").text().trim();
      String value = row.select("td").text().trim();
      rowLabels.add(label);
      rowValues.add(value);
    });

    // 3) 필요한 필드를 찾을 변수 선언
    String major = null;
    String studentId = null;
    String studentName = null;
    String academicYear = null;
    String enrollmentStatus = null;

    // 4) label/value 파싱
    //    * 학과명, 학번, 이름, 학년, 사용자 상태
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
          // 그 외 항목은 생략
          break;
      }
    }

    // 5) 로그 출력
    log.info("====================================");
    log.info("사용자 정보 파싱 결과");
    log.info("학과명(major)       : {}", major);
    log.info("학번(studentId)     : {}", studentId);
    log.info("이름(studentName)   : {}", studentName);
    log.info("학년(academicYear)  : {}", academicYear);
    log.info("사용자상태(enrollSt): {}", enrollmentStatus);
    log.info("====================================");


    parseAndLogAllInfos(html);
  }


  /**
   * OkHttpClient 빌더 메서드: SSL 무시, 쿠키 관리, 로깅 설정 포함
   */
  private OkHttpClient buildClient() throws Exception {
    // SSL 검증을 비활성화 (신뢰할 수 없는 인증서 무시)
    SSLContext sslContext = SSLContext.getInstance("SSL");
    sslContext.init(null, new TrustManager[]{trustAllManager()}, new java.security.SecureRandom());
    SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

    // 모든 호스트네임을 신뢰하도록 설정
    HostnameVerifier hostnameVerifier = (hostname, session) -> true;

    // HTTP 요청/응답 로깅 설정
    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(log::info);
    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

    // 쿠키 자동 관리 설정
    CookieManager cookieManager = new CookieManager();
    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    JavaNetCookieJar cookieJar = new JavaNetCookieJar(cookieManager);

    // OkHttpClient 빌드
    return new OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .hostnameVerifier(hostnameVerifier)
        .sslSocketFactory(sslSocketFactory, trustAllManager())
        .readTimeout(500, TimeUnit.MILLISECONDS)
        .addInterceptor(loggingInterceptor)
        .build();
  }

  /**
   * 모든 인증서를 신뢰하는 TrustManager 구현
   */
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

    // 1) 사용자 정보
    {
      log.info("==== [사용자 정보] ====");
      // "사용자 정보" 타이틀 뒤 테이블에서 tr 목록 추출
      String selector = ".b-con-box:has(h4.b-h4-tit01:contains(사용자 정보)) table.b-board-table tbody tr";
      doc.select(selector).forEach(tr -> {
        String label = tr.select("th").text().trim();
        String value = tr.select("td").text().trim();
        log.info("{} = {}", label, value);
      });
      log.info("");
    }

    // 2) 영역별 인증현황
    {
      log.info("==== [영역별 인증현황] ====");
      String selector = ".b-con-box:has(h4.b-h4-tit01:contains(영역별 인증현황)) table.b-board-table tbody tr";
      // 구분 / 이수권수 / 인증권수 (3개 컬럼)
      doc.select(selector).forEach(tr -> {
        // th: 구분, td:nth-child(1): 이수권수, td:nth-child(2): 인증권수
        // 실제 테이블 구조에 따라 인덱스를 맞춰주세요.
        String col1 = tr.select("th").text().trim();         // "서양의 역사와 사상 (4권)" 등
        String col2 = tr.select("td").first() != null
            ? tr.select("td").first().text().trim()
            : "";
        String col3 = tr.select("td").last() != null
            ? tr.select("td").last().text().trim()
            : "";
        log.info("{} | 이수권수: {} | 인증권수: {}", col1, col2, col3);
      });
      log.info("");
    }

    // 3) 인증 시험 현황
    {
      log.info("==== [인증 시험 현황] ====");
      // 년도/학기 | 영역명 | 도서명 | 응시일자 | 점수 | 합격여부 (6개 컬럼)
      String selector = ".b-con-box:has(h4.b-h4-tit01:contains(인증 시험 현황)) table.b-board-table tbody tr";
      doc.select(selector).forEach(tr -> {
        // td:nth-child(1) ~ td:nth-child(6)
        // 실제 위치에 맞게 index 조절
        List<String> cols = tr.select("td").eachText();
        if (cols.size() < 6) {
          // "검색된 결과가 없습니다"처럼 1개만 있을 수도
          log.info(tr.text());
        } else {
          log.info("년도/학기={}; 영역명={}; 도서명={}; 응시일자={}; 점수={}; 합격여부={}",
              cols.get(0), cols.get(1), cols.get(2), cols.get(3), cols.get(4), cols.get(5));
        }
      });
      log.info("");
    }

    // 4) 과목 대체 인증 현황
    {
      log.info("==== [과목 대체 인증 현황] ====");
      // 년도/학기 | 과목명 | 영역명 | 도서명 | 이수여부 (5개 컬럼)
      String selector = ".b-con-box:has(h4.b-h4-tit01:contains(과목 대체 인증 현황)) table.b-board-table tbody tr";
      doc.select(selector).forEach(tr -> {
        List<String> cols = tr.select("td").eachText();
        if (cols.size() < 5) {
          log.info(tr.text());
        } else {
          log.info("년도/학기={}; 과목명={}; 영역명={}; 도서명={}; 이수여부={}",
              cols.get(0), cols.get(1), cols.get(2), cols.get(3), cols.get(4));
        }
      });
      log.info("");
    }

    // 5) 대회 인증 현황
    {
      log.info("==== [대회 인증 현황] ====");
      // 년도/학기 | 대회명 | 영역명 | 도서명 (4개 컬럼)
      String selector = ".b-con-box:has(h4.b-h4-tit01:contains(대회 인증 현황)) table.b-board-table tbody tr";
      doc.select(selector).forEach(tr -> {
        List<String> cols = tr.select("td").eachText();
        if (cols.size() < 4) {
          log.info(tr.text());
        } else {
          log.info("년도/학기={}; 대회명={}; 영역명={}; 도서명={}",
              cols.get(0), cols.get(1), cols.get(2), cols.get(3));
        }
      });
      log.info("");
    }

    // 6) 교과연계 인증 현황
    {
      log.info("==== [교과연계 인증 현황] ====");
      // 년도/학기 | 과목명 | 영역명 | 도서명 | 이수구분 (5개 컬럼)
      String selector = ".b-con-box:has(h4.b-h4-tit01:contains(교과연계 인증 현황)) table.b-board-table tbody tr";
      doc.select(selector).forEach(tr -> {
        List<String> cols = tr.select("td").eachText();
        if (cols.size() < 5) {
          log.info(tr.text());
        } else {
          log.info("년도/학기={}; 과목명={}; 영역명={}; 도서명={}; 이수구분={}",
              cols.get(0), cols.get(1), cols.get(2), cols.get(3), cols.get(4));
        }
      });
      log.info("");
    }

    log.info("=== 모든 정보 로그 출력 완료 ===");
  }

}
