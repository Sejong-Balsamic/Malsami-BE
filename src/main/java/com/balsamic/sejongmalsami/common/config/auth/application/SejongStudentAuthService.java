package com.balsamic.sejongmalsami.common.config.auth.application;

import com.balsamic.sejongmalsami.common.config.auth.dto.request.SejongStudentAuthRequest;
import com.balsamic.sejongmalsami.common.config.auth.dto.response.SejongStudentAuthResponse;
import com.balsamic.sejongmalsami.member.domain.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class SejongStudentAuthService {

  private final MemberRepository memberRepository;

  private final Logger LOGGER = LoggerFactory.getLogger(
      SejongStudentAuthService.class);

  public SejongStudentAuthResponse getMemberAuthInfos(
      SejongStudentAuthRequest request
  ) throws IOException, IllegalArgumentException {

    String jsessionId = setJsessionId();

    sendPostToSejong(request.getSejongPortalId(), request.getSejongPortalPassword(), jsessionId);

    return sendGetToSejong(jsessionId);
  }

  private String setJsessionId() throws IOException, IllegalArgumentException {

    OkHttpClient client = new OkHttpClient().newBuilder().build();

    Request request = new Request.Builder()
        .url("http://classic.sejong.ac.kr").build();

    Response response = client.newCall(request).execute();

    Headers headers = response.headers();

    for (String name : headers.names()) {
      List<String> values = headers.values(name);
      if ("Set-Cookie".equalsIgnoreCase(name)) {
        for (String value : values) {
          if (value.contains("JSESSIONID")) {
            return extractJSessionID(value);
          }
        }
      }
    }

    return null;
  }

  private String extractJSessionID(String cookieValue) {

    String[] parts = cookieValue.split(";");

    for (String part : parts) {
      part = part.trim();
      if (part.startsWith("JSESSIONID=")) {
        return part.substring("JSESSIONID=".length());
      }
    }

    return null;
  }

  private void sendPostToSejong(String studentId, String password, String jsessionId) throws IOException, IllegalArgumentException{

    OkHttpClient client = new OkHttpClient().newBuilder().build();

    RequestBody body = new MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart("userId", studentId)
        .addFormDataPart("password", password).build();

    Request request = new Request.Builder()
        .url("https://classic.sejong.ac.kr/userLogin.do?userId=" + studentId + "&password=" + password)
        .method("POST", body)
        .addHeader("Cookie", "JSESSIONID=" + jsessionId).build();

    try (Response response = client.newCall(request).execute()) {
      LOGGER.debug("[sendPostToSejong] {}", response);
    }

  }

  private SejongStudentAuthResponse sendGetToSejong(String jsessionId) throws IOException, IllegalArgumentException {

    OkHttpClient client = new OkHttpClient().newBuilder()
        .build();

    Request request = new Request.Builder()
        .url("http://classic.sejong.ac.kr/userCertStatus.do?menuInfoId=MAIN_02_05")
        .addHeader("Cookie", "JSESSIONID=" + jsessionId)
        .build();

    try (Response response = client.newCall(request).execute()) {

      LOGGER.debug("[sendGetToSejong] {}", response);

      if (response.body() != null) {
        return extractDataFromHtml(response.body().string());
      } else
        return null;
    }
  }

  private SejongStudentAuthResponse extractDataFromHtml(String html) {

    if (html == null || html.isEmpty()) {
      throw new IllegalArgumentException("HTML content must not be null or empty");
    }
    // 추가 로그
    log.info("Extracting data from HTML: " + html);

    List<String> dataList = new ArrayList<>();
    Document doc = Jsoup.parse(html);

    Elements elements = doc.select("div.contentWrap li dl dd");

    for (Element element : elements) {
      dataList.add(element.text());
    }

    try{
      SejongStudentAuthResponse authResponse = SejongStudentAuthResponse.builder()
          .major(dataList.get(0))
          .studentIdString(dataList.get(1))
          .studentName(dataList.get(2))
          .academicYear(dataList.get(3))
          .enrollmentStatus(dataList.get(4))
          .build();

      return authResponse;
    }catch (RuntimeException exception){
      throw new IllegalArgumentException("INVALID ARGUMENT");
    }

  }
}
