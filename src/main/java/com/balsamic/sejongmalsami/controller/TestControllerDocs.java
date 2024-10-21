package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.TestCommand;
import com.balsamic.sejongmalsami.object.TestDto;
import com.balsamic.sejongmalsami.object.constants.Author;
import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;

public interface TestControllerDocs {


  @ApiChangeLogs({
      //TODO: 문서 암호화 및 메타데이터 저장, 파일 이름 unique 값 생성
      @ApiChangeLog(
          date = "2024.10.20",
          author = Author.SUHSAECHAN,
          description = "문서 파일 업로드 로직 구현"
      )
  })
  @Operation(
      summary = "자료게시판 : 자료 파일을 업로드",
      description = """
          """
  )
  public ResponseEntity<TestDto> saveDocumentThumbnail(@ModelAttribute TestCommand command);


  @ApiChangeLogs({
      @ApiChangeLog(
          //TODO: 이미지 ZIP 묶음 업로드 및 암호화, 파일 이름 unique 값 생성
          date = "2024.10.20",
          author = Author.SUHSAECHAN,
          description = "이미지 리스트 업로드시 첫번재 이미지 썸네일로 업로드 구현"
      )
  })
  @Operation(
      summary = "자료게시판 : 이미지 리스트 파일을 업로드",
      description = """
          """
  )
  public ResponseEntity<TestDto> saveImagesThumbnail(@ModelAttribute TestCommand command);
}
