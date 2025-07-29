package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.application.service.MemberService;
import com.balsamic.sejongmalsami.auth.dto.CustomUserDetails;
import com.balsamic.sejongmalsami.member.dto.MemberCommand;
import com.balsamic.sejongmalsami.member.dto.MemberDto;
import com.balsamic.sejongmalsami.util.log.LogMonitoringInvocation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
@Tag(
    name = "회원 관리 API",
    description = "회원 관리 API 제공"
)
public class MemberController implements MemberControllerDocs {

  private final MemberService memberService;

  @Override
  @LogMonitoringInvocation
  @PostMapping(value = "/my-page", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<MemberDto> myPage(
      @ModelAttribute MemberCommand command,
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    command.setMember(customUserDetails.getMember());
    return ResponseEntity.ok(memberService.myPage(command));
  }

  // 기본 회원 반환
  @Override
  @LogMonitoringInvocation
  @PostMapping(value = "/my-info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<MemberDto> myInfo(
      @ModelAttribute MemberCommand command,
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    return ResponseEntity.ok(MemberDto.builder().member(customUserDetails.getMember()).build());
  }

  // 엽전 정보 및 게시글등급 접근권한 반환
  @Override
  @LogMonitoringInvocation
  @PostMapping(value = "/yeopjeon-info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<MemberDto> getDocumentBoardAccessByTier(
      @ModelAttribute MemberCommand command,
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    command.setMember(customUserDetails.getMember());
    return ResponseEntity.ok(memberService.getDocumentBoardAccessByTier(command));
  }

  // 사용자가 작성한 글 반환
  @Override
  @LogMonitoringInvocation
  @PostMapping(value = "/my-post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<MemberDto> getAllMemberPost(
      @ModelAttribute MemberCommand command,
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    command.setMember(customUserDetails.getMember());
    return ResponseEntity.ok(memberService.getAllMemberPost(command));
  }
}