package com.balsamic.sejongmalsami.web.controller.view;

import com.balsamic.sejongmalsami.application.dto.AdminCommand;
import com.balsamic.sejongmalsami.application.dto.AdminDto;
import com.balsamic.sejongmalsami.application.service.AdminApiService;
import com.balsamic.sejongmalsami.application.service.MemberService;
import com.balsamic.sejongmalsami.auth.dto.CustomUserDetails;
import com.balsamic.sejongmalsami.auth.dto.FirebaseDto;
import com.balsamic.sejongmalsami.notice.dto.NoticePostCommand;
import com.balsamic.sejongmalsami.notice.dto.NoticePostDto;
import com.balsamic.sejongmalsami.notice.service.NoticePostService;
import com.balsamic.sejongmalsami.util.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(
    name = "관리자 WEB API",
    description = "관리자 WEB API 제공"
)
public class AdminApiController {

  private final MemberService memberService;
  private final AdminApiService adminApiService;
  private final NoticePostService noticePostService;

  /**
   * =========================================== 회원 관리자 API ===========================================
   */

  @PostMapping(value = "/member/all", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<AdminDto> getAllMembers(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute AdminCommand command) {
    return ResponseEntity.ok(memberService.getAllMembers(command));
  }

  @PostMapping(value = "/member/filter", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<AdminDto> getFilteredMembers(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute AdminCommand command) {
    return ResponseEntity.ok(adminApiService.getFilteredMembers(command));
  }

  @PostMapping(value = "/member/info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<AdminDto> getMemberByMemberIdStr(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute AdminCommand command) {
    return ResponseEntity.ok(adminApiService.getMemberByMemberIdStr(command));
  }

  /**
   * =========================================== 테스트 계정 API ===========================================
   */

  @PostMapping(value = "/test/account/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<AdminDto> createTestMember(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute AdminCommand command) {
    command.setMember(customUserDetails.getMember());
    return ResponseEntity.ok(adminApiService.createTestMember(command));
  }

  @PostMapping(value = "/test/account/get-all", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<AdminDto> getFilteredTestMembers(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute AdminCommand command) {
    return ResponseEntity.ok(adminApiService.getFilteredTestMembers(command));
  }

  /**
   * =========================================== 엽전 관리 API ===========================================
   */

  @PostMapping(value = "/yeopjeon/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<AdminDto> manageYeopjeon(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute AdminCommand command) {
    command.setMember(customUserDetails.getMember());
    return ResponseEntity.ok(adminApiService.manageYeopjeon(command));
  }

  @PostMapping(value = "/yeopjeon/my-info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<AdminDto> getMyYeopjeonInfo(
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    return ResponseEntity.ok(adminApiService.getMyYeopjeonInfo(customUserDetails.getMember()));
  }

  @PostMapping(value = "/yeopjeon/search", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<AdminDto> getFilteredMembersAndYeopjeons(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute AdminCommand command) {
    command.setMember(customUserDetails.getMember());
    return ResponseEntity.ok(adminApiService.getFilteredMembersAndYeopjeons(command));
  }


  /**
   * =========================================== 개발자 놀이터 ===========================================
   */

  // uuid 닉네임 랜덤 뽑기
  @PostMapping(value = "/uuid-ppchko")
  @LogMonitoringInvocation
  public ResponseEntity<AdminDto> processUuidPacchingko(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute AdminCommand command) {
    command.setMember(customUserDetails.getMember());
    return ResponseEntity.ok(adminApiService.processUuidPacchingko(command));
  }

  /**
   * =========================================== 교과목 관리 API ===========================================
   */

  // 교과목 필터링
  @PostMapping(value = "/subject/filter")
  @LogMonitoringInvocation
  public ResponseEntity<AdminDto> getFilteredSubjects(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute AdminCommand command
  ) {
    command.setMember(customUserDetails.getMember());
    return ResponseEntity.ok(adminApiService.getFilteredSubject(command));
  }

  // 교과목 자동완성
  @PostMapping(value = "/subject/autocomplete")
  public ResponseEntity<AdminDto> getSubjectAutocomplete(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute AdminCommand command
  ) {
    command.setMember(customUserDetails.getMember());
    return ResponseEntity.ok(adminApiService.subjectAutoComplete(command));
  }

  // 교과목 엑셀파일 업로드
  @PostMapping(value = "/subject/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<AdminDto> uploadCourseFile(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute AdminCommand command
  ) {
    command.setMember(customUserDetails.getMember());
    return ResponseEntity.ok(adminApiService.uploadCourseFile(command));
  }

  // 교과목 엑셀파일 필터링 조회
  @PostMapping(value = "/course-file/filter", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<AdminDto> getFilteredCourseFile(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute AdminCommand command
  ) {
    command.setMember(customUserDetails.getMember());
    return ResponseEntity.ok(adminApiService.getFilteredCourseFile(command));
  }

  // 교과목 엑셀파일 다운로드
  @PostMapping(value = "/course-file/download", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<byte[]> downloadCourseFile(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute AdminCommand command
  ) {
    command.setMember(customUserDetails.getMember());
    AdminDto dto = adminApiService.downloadCourseFile(command);

    // Content-Disposition 설정 (파일 이름 안전하게 인코딩)
    ContentDisposition contentDisposition = ContentDisposition
        .attachment()
        .filename(dto.getFileName(), StandardCharsets.UTF_8)
        .build();

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
        .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .body(dto.getFileBytes());
  }

  /**
   * =========================================== 에러코드 관리 API ===========================================
   */
  @PostMapping(value = "/error-code/search")
  @LogMonitoringInvocation
  public ResponseEntity<AdminDto> getFilteredServerErrorCode(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute AdminCommand command) {
    command.setMember(customUserDetails.getMember());
    return ResponseEntity.ok(adminApiService.getFilteredServerErrorCode(command));
  }

  /**
   * =========================================== 공지사항 관리 API ===========================================
   */
  @PostMapping(value = "/notice/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<NoticePostDto> saveNoticePost(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute NoticePostCommand command
  ) {
    command.setMember(customUserDetails.getMember());
    return ResponseEntity.ok(adminApiService.saveNoticePost(command));
  }

  @PostMapping(value = "/notice/pin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<Void> pinNoticePost(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute NoticePostCommand command
  ) {
    command.setMember(customUserDetails.getMember());
    adminApiService.pinNoticePost(command);
    return ResponseEntity.ok().build();
  }

  /**
   * =========================================== 질문 게시글 관리 API ===========================================
   */
  @PostMapping(value = "/question-post/filter")
  @LogMonitoringInvocation
  public ResponseEntity<AdminDto> getFilteredQuestionPost(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute AdminCommand command) {
    command.setMember(customUserDetails.getMember());
    return ResponseEntity.ok(adminApiService.getFilteredQuestionPost(command));
  }

  /**
   * =========================================== 알림 API ===========================================
   */
  @PostMapping(value = "/firebase")
  @LogMonitoringInvocation
  public ResponseEntity<FirebaseDto> getFirebaseConfig() {
    return ResponseEntity.ok(adminApiService.getFirebaseProperties());
  }
}
