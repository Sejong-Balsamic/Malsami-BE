package com.balsamic.sejongmalsami.controller.view;

import com.balsamic.sejongmalsami.object.AdminCommand;
import com.balsamic.sejongmalsami.object.AdminDto;
import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.MemberCommand;
import com.balsamic.sejongmalsami.object.MemberDto;
import com.balsamic.sejongmalsami.service.AdminApiService;
import com.balsamic.sejongmalsami.service.MemberService;
import com.balsamic.sejongmalsami.util.log.LogMonitoringInvocation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class AdminApiController {

  private final MemberService memberService;
  private final AdminApiService adminApiService;
  /**
   * //TODO: url 수정 필요
   *변경안
   * /admin/members/list : 모든 회원 조회
   * /admin/members/search : 필터 조건으로 회원 조회
   * 변경안
   * /admin/test-accounts/create : 테스트 계정 생성 (account에서 accounts로 복수화를 통해 리소스 명확화)
   * /admin/test-accounts/list : 테스트 계정 리스트 조회
   * 변경안
   * /admin/yeopjeon/manage : 엽전 증감, 관리 동작 (단일 엔드포인트로 관리 기능을 처리)
   * /admin/yeopjeon/history 또는 /admin/yeopjeon/transactions : 엽전 관리 이력 조회
   * 변경안
   * /admin/dev-tools/generate-random-uuid-nickname
   * 좀 길지만 기능을 정확히 드러냄.
   * 줄이고 싶다면 /admin/dev-tools/uuid-nickname 정도도 가능.
   *
   *
   * @PostMapping(value = "/members/list", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
   * public ResponseEntity<MemberDto> listAllMembers(...)
   *
   * @PostMapping(value = "/members/search", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
   * public ResponseEntity<MemberDto> searchMembers(...)
   *
   * @PostMapping(value = "/test-accounts/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
   * public ResponseEntity<MemberDto> createTestMember(...)
   *
   * @PostMapping(value = "/test-accounts/list", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
   * public ResponseEntity<MemberDto> listTestMembers(...)
   *
   * @PostMapping(value = "/yeopjeon/manage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
   * public ResponseEntity<YeopjeonDto> manageYeopjeon(...)
   *
   * // 이력이 필요하다면 별도 추가
   * @PostMapping(value = "/yeopjeon/history", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
   * public ResponseEntity<YeopjeonHistoryDto> yeopjeonHistory(...)
   *
   * @PostMapping(value = "/dev-tools/uuid-nickname", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
   * public ResponseEntity<AdminDto> generateRandomUuidNickname(...)
   */

  /**
   * =========================================== 회원 관리자 API ===========================================
   */

  @PostMapping(value = "/member/all", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<MemberDto> getAllMembers(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute MemberCommand command) {
    return ResponseEntity.ok(memberService.getAllMembers(command));
  }

  @PostMapping(value = "/member/filter", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<MemberDto> getFilteredMembers(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute MemberCommand command) {
    return ResponseEntity.ok(adminApiService.getFilteredMembers(command));
  }

  /**
   * =========================================== 테스트 계정 API ===========================================
   */

  @PostMapping(value = "/test/account/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<MemberDto> createTestMember(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute MemberCommand command) {
    command.setMember(customUserDetails.getMember());
    return ResponseEntity.ok(adminApiService.createTestMember(command));
  }

  @PostMapping(value = "/test/account/get-all", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<MemberDto> getFilteredTestMembers(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute MemberCommand command) {
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
    return ResponseEntity.ok(adminApiService.getFilteredSubjects(command));
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
  public ResponseEntity<AdminDto> uploadCourseExcelFile(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute AdminCommand command
  ) {
    command.setMember(customUserDetails.getMember());
    return ResponseEntity.ok(adminApiService.uploadCourseExcelFile(command));
  }
}
