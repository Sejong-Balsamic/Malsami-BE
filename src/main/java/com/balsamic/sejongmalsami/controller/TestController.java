//package com.balsamic.sejongmalsami.controller;
//
//import com.balsamic.sejongmalsami.object.CustomUserDetails;
//import com.balsamic.sejongmalsami.object.QuestionCommand;
//import com.balsamic.sejongmalsami.object.QuestionDto;
//import com.balsamic.sejongmalsami.object.TestCommand;
//import com.balsamic.sejongmalsami.object.TestDto;
//import com.balsamic.sejongmalsami.object.constants.Author;
//import com.balsamic.sejongmalsami.service.TestService;
//import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
//import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
//import com.balsamic.sejongmalsami.util.log.LogMonitoringInvocation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.ModelAttribute;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/test")
//@Tag(
//    name = "개발자용 TEST API",
//    description = "TEST API 제공"
//)
//public class TestController implements TestControllerDocs {
//
//  private final TestService testService;;
//
//  @PostMapping(value = "/thumbnail/save-document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//  @LogMonitoringInvocation
//  @Override
//  public ResponseEntity<TestDto> saveDocumentThumbnail(@ModelAttribute TestCommand command) {
//    return ResponseEntity.ok(testService.saveDocumentThumbnail(command));
//  }
//
//
//  @PostMapping(value = "/thumbnail/save-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//  @LogMonitoringInvocation
//  @Override
//  public ResponseEntity<TestDto> saveImagesThumbnail(@ModelAttribute TestCommand command) {
//    return ResponseEntity.ok(testService.saveImagesThumbnail(command));
//  }
//
//}
