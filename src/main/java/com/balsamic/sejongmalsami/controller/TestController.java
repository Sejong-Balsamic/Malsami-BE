package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.service.TestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
@Tag(
    name = "개발자용 TEST API",
    description = "TEST API 제공"
)
public class TestController {

  private final TestService testService;

  @Operation(summary = "질문 글 Mock 데이터 생성",
  description = """
      ### 질문 글 및 답변 글 Mock 데이터 생성
                                                                                 
       이 엔드포인트는 테스트 목적으로 임의의 질문 글과 그에 따른 답변 글 데이터를 생성합니다. 지정된 개수만큼의 질문 글을 생성하며, 각 질문 글에는 랜덤하게 0개에서 10개 사이의 답변 글이 작성됩니다.
       또한 각 질문 글 및 답변 글에는 랜덤하게 0개에서 5개 사이의 댓글이 작성됩니다.
  
       **동작 방식:**
  
       1. **사용자 생성:** 새로운 Mock 사용자를 생성합니다.
       2. **질문 글 생성:** 각 사용자에게 랜덤한 수의 질문 글을 생성합니다.
       3. **답변 글 생성:** 각 질문 글에 대해 0개에서 10개 사이의 랜덤한 개수의 답변 글을 생성합니다.
           - **답변 작성자:** 답변 글의 작성자는 해당 질문 글의 작성자와 다릅니다.
           - **채택 여부:** 여러 답변 중 단 하나의 답변만 `isChaetaek = true`로 설정됩니다. 채택된 답변이 없을 수도 있습니다.
       4. **댓글 생성** 각 질문 글 및 답변 글에 0개에서 5개 사이의 랜덤한 개수의 댓글을 생성합니다.
       5. **반복:** 총 생성된 질문 글의 개수가 `postCount`에 도달할 때까지 위 과정을 반복합니다.
  
       **매개변수:**
  
       - `postCount` (선택 사항): 생성할 질문 글의 총 개수입니다. 값이 제공되지 않으면 기본값으로 **30개**가 설정됩니다.
  
       **사용 예시:**
  
       - `postCount`가 **50**인 경우, 50개의 질문 글과 각각에 대응하는 답변 글 및 댓글이 생성됩니다.
       - `postCount`를 지정하지 않으면, 기본적으로 30개의 질문 글과 그에 따른 답변 글 및 댓글이 생성됩니다.

       **주의사항:**
  
       - `postCount`는 **양의 정수**여야 합니다. 음수나 0이 입력될 경우, 기본값이 적용되거나 오류가 발생할 수 있습니다.
       - 대량의 데이터를 생성할 경우, 시스템 성능에 영향을 줄 수 있으므로 적절한 값을 설정하는 것이 좋습니다.
      """)
  @PostMapping( "/create/question/board")
  public void createMockQuestionPostAndAnswerPost(Integer postCount) {
    testService.createMockQuestionPostAndAnswerPost(postCount);
  }

  @Operation(
      summary = "문서 글 및 파일 Mock 데이터 생성",
      description = """
            ### 문서 글 및 파일 Mock 데이터 생성
                                                                                         
            이 엔드포인트는 테스트 목적으로 임의의 문서 글과 그에 따른 파일 데이터를 생성합니다. 지정된 개수만큼의 문서 글을 생성하며, 각 문서 글에는 랜덤하게 0개에서 5개 사이의 파일이 첨부됩니다.

            **동작 방식:**

            1. **사용자 풀 생성:** 일정 수의 Mock 사용자를 미리 생성하여 문서 글 작성 시 랜덤으로 선택합니다.
            2. **문서 글 생성:** 각 사용자에게 랜덤한 수의 문서 글을 생성합니다.
            3. **파일 생성:** 각 문서 글에 대해 0개에서 5개 사이의 랜덤한 개수의 파일을 생성합니다.
                - **파일 업로더:** 파일의 업로더는 사용자 풀에서 랜덤으로 선택됩니다.
            4. **반복:** 총 생성된 문서 글의 개수가 `postCount`에 도달할 때까지 위 과정을 반복합니다.

            **매개변수:**

            - `postCount` (선택 사항): 생성할 문서 글의 총 개수입니다. 값이 제공되지 않으면 기본값으로 **30개**가 설정됩니다.

            **사용 예시:**

            - `postCount`가 **50**인 경우, 50개의 문서 글과 각각에 대응하는 파일이 생성됩니다.
            - `postCount`를 지정하지 않으면, 기본적으로 30개의 문서 글과 그에 따른 파일이 생성됩니다.

            **주의사항:**

            - `postCount`는 **양의 정수**여야 합니다. 음수나 0이 입력될 경우, 기본값이 적용되거나 오류가 발생할 수 있습니다.
            - 대량의 데이터를 생성할 경우, 시스템 성능에 영향을 줄 수 있으므로 적절한 값을 설정하는 것이 좋습니다.
        """
  )
  @PostMapping( "/create/document/board")
  public void createMockDocumentPostAndAnswerPost(Integer postCount) {
    testService.createMockDocumentPostAndDocumentFiles(postCount);
  }

  @Operation(summary = "자료 요청 글 Mock 데이터 생성",
      description = """
      ### 자료 요청 글 Mock 데이터 생성
                                                                                 
       이 엔드포인트는 테스트 목적으로 임의의 자료 요청 글과 그에 따른 댓글 데이터를 생성합니다. 지정된 개수만큼의 자료 요청 글을 생성하며, 각 질문 글에는 랜덤하게 0개에서 5개 사이의 댓글이 작성됩니다.
  
       **동작 방식:**
  
       1. **사용자 생성:** 새로운 Mock 사용자를 생성합니다.
       2. **자료 요청 글 생성:** 각 사용자에게 랜덤한 수의 자료 요청 글을 생성합니다.
       3. **댓글 생성:** 각 질문 글에 대해 0개에서 5개 사이의 랜덤한 개수의 댓글을 생성합니다.
           - **댓글 작성자:** 댓글 작성자는 해당 자료 요청 글의 작성자와 다릅니다.
       4. **반복:** 총 생성된 자료 요청 글의 개수가 `postCount`에 도달할 때까지 위 과정을 반복합니다.
  
       **매개변수**
  
       - `postCount` (선택 사항): 생성할 글의 총 개수입니다. 값이 제공되지 않으면 기본값으로 **30개**가 설정됩니다.
  
       **사용 예시:**
  
       - `postCount`가 **50**인 경우, 50개의 자료 요청 글과 각각에 대응하는 댓글이 생성됩니다.
       - `postCount`를 지정하지 않으면, 기본적으로 30개의 자료 요청 글과 그에 따른 댓글이 생성됩니다.

       **주의사항:**
  
       - `postCount`는 **양의 정수**여야 합니다. 음수나 0이 입력될 경우, 기본값이 적용되거나 오류가 발생할 수 있습니다.
       - 대량의 데이터를 생성할 경우, 시스템 성능에 영향을 줄 수 있으므로 적절한 값을 설정하는 것이 좋습니다.
      """)
  @PostMapping( "/create/document/request/board")
  public void createMockDocumentRequestPost(Integer postCount) {
    testService.createMockDocumentRequestPost(postCount);
  }
}
