package com.balsamic.sejongmalsami.web.controller.api;

import com.balsamic.sejongmalsami.ai.dto.EmbeddingCommand;
import com.balsamic.sejongmalsami.auth.dto.CustomUserDetails;
import com.balsamic.sejongmalsami.post.dto.EmbeddingDto;
import com.balsamic.sejongmalsami.post.service.PostEmbeddingService;
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
@RequestMapping("/api/ai/")
@Tag(
    name = "AI 관리 API",
    description = "AI 관리 API 제공"
)
public class AIController implements AIControllerDocs {
  private final PostEmbeddingService postEmbeddingService;

  @PostMapping(value = "/search", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<EmbeddingDto> searchSimilarEmbeddings(
      @ModelAttribute EmbeddingCommand command,
      @AuthenticationPrincipal CustomUserDetails customUserDetails) {
    return ResponseEntity.ok(postEmbeddingService.searchSimilarEmbeddingsByText(command));
  }
}
