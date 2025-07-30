package com.balsamic.sejongmalsami.web.controller;

import com.balsamic.sejongmalsami.ai.dto.EmbeddingCommand;
import com.balsamic.sejongmalsami.auth.dto.CustomUserDetails;
import com.balsamic.sejongmalsami.constants.Author;
import com.balsamic.sejongmalsami.post.dto.EmbeddingDto;
import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;

public interface AIControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.01.03",
          author = Author.SUHSAECHAN,
          description = "AI 유사도 검색 구현"
      )
  })
  @Operation(
      summary = "유사 Embedding 검색",
      description = """
          **유사 Embedding 검색 API**

          이 API는 제공된 텍스트를 기반으로 유사한 Embedding을 검색합니다. 
          사용자는 텍스트 입력과 함께 임계값(threshold) 및 콘텐츠 유형(contentType)을 지정할 수 있습니다.

          ### **인증 정보**

          - **API는 인증이 필요: 유효한 JWT 토큰 필요**
          
          ### **요청 파라미터**

          - **EmbeddingCommand**
              - **String text**: 검색에 사용할 텍스트
                _예: "예시 텍스트"_
              - **Float threshold**: 유사도 임계값
                _예: 0.75_
              - **String contentType**: 검색할 콘텐츠 유형
                _예: "DOCUMENT", "QUESTION"_
              - **Integer pageNumber**: 페이지 번호 (기본값: 0)
              - **Integer pageSize**: 페이지당 항목 수 (기본값: 10)
                
          ### 추천 threshold 값 가이드:
          - 0.95: 거의 동일한 내용 검색
          - 0.90: 매우 유사한 내용 검색
          - 0.85: 유사한 내용 검색
          - 0.80: 관련성 있는 내용 검색
          - 0.75 이하: 느슨한 관련성 검색

          ---
          ** 추천 프로젝트 구성값 **: \s
          - **Question 검색**: 다양한 결과 허용 → `Threshold: 0.7~0.8` 추천. \s
          - **Document 검색**: 정확성 강조 → `Threshold: 0.8~0.9` 추천.
          - 필요에 따라 Threshold 값을 조정하며 테스트
          
          ---
            ### **참고 사항**
            - 이 API는 비동기적으로 Embedding을 생성하고 검색합니다.
            - `threshold` 값은 0과 1 사이의 값을 권장합니다.
            - `contentType`은 사전에 정의된 콘텐츠 유형이어야 합니다.
            - 페이징 파라미터(`pageNumber`, `pageSize`)는 선택 사항이며 기본값이 적용됩니다.
            """
  )
  ResponseEntity<EmbeddingDto> searchSimilarEmbeddings(
      @ModelAttribute EmbeddingCommand command,
      @AuthenticationPrincipal CustomUserDetails customUserDetails);
}
