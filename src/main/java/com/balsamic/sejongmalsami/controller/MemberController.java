package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.MemberCommand;
import com.balsamic.sejongmalsami.object.MemberDto;
import com.balsamic.sejongmalsami.service.MemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
public class MemberController implements MemberControllerDocs{
  private final MemberService memberService;

  @Override
  @PostMapping(value = "/signin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<MemberDto> signIn(
      @ModelAttribute MemberCommand command){
    return ResponseEntity.ok(memberService.signIn(command));
  }
}