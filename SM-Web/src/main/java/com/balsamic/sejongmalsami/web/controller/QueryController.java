package com.balsamic.sejongmalsami.web.controller;

import com.balsamic.sejongmalsami.object.QueryCommand;
import com.balsamic.sejongmalsami.post.dto.QueryDto;
import com.balsamic.sejongmalsami.post.service.QueryService;
import com.balsamic.sejongmalsami.post.service.SearchHistoryService;
import com.balsamic.sejongmalsami.util.log.LogMonitoringInvocation;
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
@RequestMapping("/api/query")
@Tag(
    name = "검색 페이지 API",
    description = "검색 페이지 관련 API 제공"
)
public class QueryController implements QueryControllerDocs {

  private final QueryService queryService;
  private final SearchHistoryService searchHistoryService;

  @Override
  @PostMapping(value = "", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<QueryDto> getPostsByQuery(
      @ModelAttribute QueryCommand command) {
    return ResponseEntity.ok(queryService.getPostsByQuery(command));
  }

  @Override
  @PostMapping(value = "/popular", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @LogMonitoringInvocation
  public ResponseEntity<QueryDto> getTopKeywords(
      @ModelAttribute QueryCommand command) {
    return ResponseEntity.ok(searchHistoryService.getRealTimeTopKeywords(command));
  }
}
