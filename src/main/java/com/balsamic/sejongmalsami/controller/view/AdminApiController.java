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
   * ===========================================
   * 회원 관리자 API
   * ===========================================
   */

  @PostMapping(value = "/member/all", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<MemberDto> getAllMembers(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute MemberCommand command){
    return ResponseEntity.ok(memberService.getAllMembers(command));
  }

  @PostMapping(value = "/member/filter", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<MemberDto> getFilteredMembers(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute MemberCommand command){
    return ResponseEntity.ok(memberService.getFilteredMembers(command));
  }

  /**
   * ===========================================
   * 테스트 계정 API
   * ===========================================
   */

  @PostMapping(value = "/test/account/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<MemberDto> createTestMember(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute MemberCommand command){
    command.setMember(customUserDetails.getMember());
    return ResponseEntity.ok(adminApiService.createTestMember(command));
  }

  @PostMapping(value = "/test/account/get-all", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<MemberDto> getFilteredTestMembers(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute MemberCommand command){
    return ResponseEntity.ok(adminApiService.getFilteredTestMembers(command));
  }

  /**
   * ===========================================
   * 개발자 놀이터
   * ===========================================
   */

  // uuid 닉네임 랜덤 뽑기
  @PostMapping(value = "/uuid-ppchko")
  @LogMonitoringInvocation
  public ResponseEntity<AdminDto> processUuidPacchingko(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute AdminCommand command){
    command.setMember(customUserDetails.getMember());
    return ResponseEntity.ok(adminApiService.processUuidPacchingko(command));
  }

}
