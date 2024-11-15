package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.MemberCommand;
import com.balsamic.sejongmalsami.object.WebLoginDto;
import com.balsamic.sejongmalsami.object.constants.Author;
import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;

public interface AdminAuthControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.1l.15",
          author = Author.SUHSAECHAN,
          description = "관리자 웹 로그인 구현 (기존 로직 재사용)"
      )
  })
  @Operation(
      summary = "관리자 웹 로그인 API",
      description = """
          """
  )
  public ResponseEntity<WebLoginDto> webLogin(@ModelAttribute MemberCommand command, HttpServletResponse response);
}
