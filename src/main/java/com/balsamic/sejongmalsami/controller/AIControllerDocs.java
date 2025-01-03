package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.EmbeddingCommand;
import com.balsamic.sejongmalsami.object.EmbeddingDto;
import com.balsamic.sejongmalsami.object.constants.Author;
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
                
            ## Threshold 설정 추천
            ### 0.7~0.9
            - **일반적으로 가장 적합한 범위로 추천됩니다.**
            - 이 범위는 대부분의 검색 시 "충분히 유사한" 결과를 제공합니다.
            - **0.8**: 유사성이 높은 데이터만 필터링
            - **0.7**: 약간 더 넓은 범위에서 유사성을 판단
            ### 0.5~0.7
            - **유사도 검색 범위를 확장하고 싶을 때 사용합니다.**
            - 텍스트 데이터가 다양하고 특정 주제에 국한되지 않는 경우 유용합니다.
            - 질문-답변 데이터처럼 비슷한 의미를 가진 다양한 텍스트를 비교할 때
            ### 0.9 이상
            - **매우 높은 유사성을 가진 데이터만 필요할 때 사용합니다.**
            - 검색 결과가 엄격히 제한되므로, 유사성이 매우 높아야만 포함됩니다.

          ---

          ### 1. **Question 검색**
          #### **사용 목적**
          - 질문 게시글에서 유사한 질문을 추천하거나 기존 질문 중 답변을 확인할 수 있도록 돕는 기능.
          - 검색 결과는 **정확도보다 다양성**을 더 중시할 가능성이 높음.
          #### **추천 Threshold 값**
          - **0.7~0.8**:
            - 질문 텍스트와 유사한 기존 질문을 넓게 탐색.
            - "질문 내용 + 키워드" 조합이 중요하므로 약간 낮은 임계값으로 다양한 결과를 반환.
          #### **추천 사용 예시**
          - **질문 추천**: `Threshold: 0.7` → 다양한 질문 추천.
          - **질문 중복 방지**: `Threshold: 0.8` → 정확도가 더 높은 결과를 사용하여 중복 질문 차단.
          
          ### 2. **Document 검색**
          #### **사용 목적**
          - 사용자가 필요한 자료를 효율적으로 찾도록 지원.
          - 검색 결과의 **정확도**가 더 중요한 요소.
          #### **추천 Threshold 값**
          - **0.8~0.9**:
            - 학습 자료는 신뢰성과 정밀도가 중요하므로 높은 Threshold를 설정.
            - 검색 결과의 정확성을 보장하여 사용자가 필요한 자료만 노출.
          #### **추천 사용 예시**
          - **관련 문서 찾기**: `Threshold: 0.85` → 주제가 매우 유사한 문서만 반환.
          - **자료 추천**: `Threshold: 0.9` → 검색 기준에 매우 근접한 자료만 추천.

          ### 3. **각 검색에서 Threshold를 조정해야 하는 경우**
          - **문제**: 결과가 부족한 경우 → Threshold 값을 **낮춤**.
            - 예: Question에서 `0.7` 대신 `0.6` 사용.
          - **문제**: 결과가 너무 많거나 관련 없는 결과 포함 → Threshold 값을 **높임**.
            - 예: Document에서 `0.85` 대신 `0.9` 사용.

          **결론**: \s
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
