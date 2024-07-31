package com.balsamic.sejongmalsami.common.auth.application;

import com.balsamic.sejongmalsami.common.auth.dto.request.SejongStudentAuthRequest;
import com.balsamic.sejongmalsami.common.auth.dto.response.SejongStudentAuthResponse;
import com.balsamic.sejongmalsami.member.domain.repository.MemberRepository;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SejongStudentAuthService {

  private final MemberRepository memberRepository;
  private static final OkHttpClient CLIENT = new OkHttpClient();

  public SejongStudentAuthResponse getMemberAuthInfos(
      SejongStudentAuthRequest request) throws IOException {
    String jsessionId = obtainJSessionId();
    if (jsessionId == null) {
      throw new IllegalStateException("Cannot retrieve JSESSIONID");
    }
    performLogin(request.getSejongPortalId(), request.getSejongPortalPassword(), jsessionId);
    return fetchStudentData(jsessionId);
  }

  private String obtainJSessionId() throws IOException {
    Request request = new Request.Builder().url("http://classic.sejong.ac.kr").build();
    try (Response response = CLIENT.newCall(request).execute()) {
      return parseJSessionId(response);
    }
  }

  private String parseJSessionId(Response response) {
    if (!response.isSuccessful()) {
      log.error("Failed to connect to the login page, status: {}", response.code());
      return null;
    }
    return response.headers().values("Set-Cookie").stream()
        .filter(cookie -> cookie.startsWith("JSESSIONID"))
        .findFirst()
        .map(cookie -> cookie.split(";")[0].split("=")[1])
        .orElse(null);
  }

  private void performLogin(String studentId, String password, String jsessionId) throws IOException {
    RequestBody body = new FormBody.Builder()
        .add("userId", studentId)
        .add("password", password)
        .build();
    Request request = new Request.Builder()
        .url("https://classic.sejong.ac.kr/userLogin.do")
        .post(body)
        .addHeader("Cookie", "JSESSIONID=" + jsessionId)
        .build();

    try (Response response = CLIENT.newCall(request).execute()) {
      String responseBody = response.body() != null ? response.body().string() : "";
      if (!response.isSuccessful()) {
        log.error("Login failed, status: {}", response.code());
        throw new IOException("Failed to login, HTTP status: " + response.code());
      }

      // 로그인 실패 메시지를 찾는 로직 추가
      if(responseBody.contains("로그인 정보가 올바르지 않습니다.")) {
        log.error("Login failed, response body contains error message about invalid credentials.");
        throw new IOException("Login failed: Invalid credentials");
      }

      log.info("Login successful, HTTP status: {}", response.code());
    }
  }



  private SejongStudentAuthResponse fetchStudentData(String jsessionId) throws IOException {
    Request request = new Request.Builder()
        .url("http://classic.sejong.ac.kr/userCertStatus.do?menuInfoId=MAIN_02_05")
        .addHeader("Cookie", "JSESSIONID=" + jsessionId)
        .build();

    try (Response response = CLIENT.newCall(request).execute()) {
      if (!response.isSuccessful() || response.body() == null) {
        log.error("Failed to fetch student data, status: {}", response.code());
        throw new IOException("Failed to retrieve student data");
      }
      return parseStudentData(response.body().string());
    }
  }

  private SejongStudentAuthResponse parseStudentData(String html) {
    Document doc = Jsoup.parse(html);
    log.debug(doc.body().toString());
    Elements elements = doc.select("ul.tblA > li > dl > dd");

    List<String> dataList = new ArrayList<>();
    elements.forEach(element -> dataList.add(element.text().trim()));

    try {
      return SejongStudentAuthResponse.builder()
          .major(dataList.get(0))
          .studentIdString(dataList.get(1))
          .studentName(dataList.get(2))
          .academicYear(dataList.get(3))
          .enrollmentStatus(dataList.get(4))
          .build();
    } catch (IndexOutOfBoundsException e) {
      throw new IllegalArgumentException("Failed to parse HTML: insufficient data", e);
    }
  }
}
