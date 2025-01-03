package com.balsamic.sejongmalsami.util.deprecated;

import com.balsamic.sejongmalsami.object.MemberCommand;
import com.balsamic.sejongmalsami.object.MemberDto;
import com.balsamic.sejongmalsami.object.postgres.TestMember;
import com.balsamic.sejongmalsami.repository.postgres.TestMemberRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// 이전 대양휴머니티칼리지 로그인 (2024.12.30 ~ )
@Deprecated
@Slf4j
@Component
@RequiredArgsConstructor
public class SejongPortalAuthenticator_Deprecated {

  private static final OkHttpClient CLIENT = new OkHttpClient();
  private final TestMemberRepository testMemberRepository;
  private final PasswordEncoder passwordEncoder;

  public MemberDto getMemberAuthInfos(MemberCommand command) {

    String sejongPortalId = command.getSejongPortalId();
    String sejongPassword = command.getSejongPortalPassword();

    // 테스트 계정 인증 - 학번이 9로 시작하는경우
    if (sejongPortalId.startsWith("9")) {
      return getTestInfos(sejongPortalId, sejongPassword);
    }

    // 실제 포털 인증
    try {
      String jsessionId = obtainJSessionId();
      if (jsessionId == null) {
        throw new CustomException(ErrorCode.SEJONG_AUTH_SESSION_ERROR);
      }
      performLogin(command.getSejongPortalId(), command.getSejongPortalPassword(), jsessionId);
      return fetchStudentData(jsessionId);
    } catch (IOException e) {
      log.error("로그인 실패: 인증 중 오류가 발생했습니다.", e);
      throw new CustomException(ErrorCode.SEJONG_AUTH_CONNECTION_ERROR);
    }
  }

  private String obtainJSessionId() throws IOException {
    Request request = new Request.Builder().url("http://classic.sejong.ac.kr").build();
    try (Response response = CLIENT.newCall(request).execute()) {
      return parseJSessionId(response);
    } catch (IOException e) {
      log.error("세션 ID 가져오기 실패: 연결 중 오류가 발생했습니다.", e);
      throw new CustomException(ErrorCode.SEJONG_AUTH_SESSION_ERROR);
    }
  }

  private String parseJSessionId(Response response) {
    if (!response.isSuccessful()) {
      log.error("로그인 페이지 연결에 실패했습니다. 상태 코드: {}", response.code());
      throw new CustomException(ErrorCode.SEJONG_AUTH_CONNECTION_ERROR);
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
        log.error("로그인 실패, 상태 코드: {}", response.code());
        throw new CustomException(ErrorCode.SEJONG_AUTH_CONNECTION_ERROR);
      }

      if (responseBody.contains("로그인 정보가 올바르지 않습니다.")) {
        log.error("로그인 실패: 응답 본문에 잘못된 자격 증명 메시지가 포함되어 있습니다.");
        throw new CustomException(ErrorCode.SEJONG_AUTH_CREDENTIALS_INVALID);
      }

      log.info("로그인 성공, HTTP 상태 코드: {}", response.code());
    }
  }

  private MemberDto fetchStudentData(String jsessionId) throws IOException {
    Request request = new Request.Builder()
        .url("http://classic.sejong.ac.kr/userCertStatus.do?menuInfoId=MAIN_02_05")
        .addHeader("Cookie", "JSESSIONID=" + jsessionId)
        .build();

    try (Response response = CLIENT.newCall(request).execute()) {
      if (!response.isSuccessful() || response.body() == null) {
        log.error("학생 데이터 가져오기 실패, 상태 코드: {}", response.code());
        throw new CustomException(ErrorCode.SEJONG_AUTH_DATA_FETCH_ERROR);
      }
      return parseStudentData(response.body().string());
    }
  }

  private MemberDto parseStudentData(String html) {
    Document doc = Jsoup.parse(html);
    log.debug(doc.body().toString());
    Elements elements = doc.select("ul.tblA > li > dl > dd");

    List<String> dataList = new ArrayList<>();
    elements.forEach(element -> dataList.add(element.text().trim()));

    try {
      return MemberDto.builder()
          .major(dataList.get(0))
          .studentIdString(dataList.get(1))
          .studentName(dataList.get(2))
          .academicYear(dataList.get(3))
          .enrollmentStatus(dataList.get(4))
          .build();
    } catch (IndexOutOfBoundsException e) {
      log.error("HTML 파싱 실패: 데이터가 부족합니다.", e);
      throw new CustomException(ErrorCode.SEJONG_AUTH_DATA_FETCH_ERROR);
    }
  }


  // 테스트 계정 확인
  private MemberDto getTestInfos(String testMemberStudentId, String testMemberPassword) {
    // 테스트 계정 불러오기
    Long testStudentId = Long.parseLong(testMemberStudentId);

    TestMember testMember = testMemberRepository.findByTestStudentId(testStudentId)
        .orElseThrow(() -> new CustomException(ErrorCode.TEST_MEMBER_NOT_FOUND));

    // 비밀번호 확인
    if (!passwordEncoder.matches(testMemberPassword, testMember.getPassword())) {
      log.error("테스트 계정 비밀번호가 일치하지 않습니다: {}", testStudentId);
      throw new CustomException(ErrorCode.TEST_MEMBER_AUTH_CREDENTIALS_INVALID);
    }

    return MemberDto.builder()
        .major(testMember.getTestMajor())
        .studentIdString(testMember.getTestStudentId().toString())
        .studentName(testMember.getTestStudentName())
        .academicYear(testMember.getTestAcademicYear())
        .enrollmentStatus(testMember.getTestEnrollmentStatus())
        .build();
  }
}
