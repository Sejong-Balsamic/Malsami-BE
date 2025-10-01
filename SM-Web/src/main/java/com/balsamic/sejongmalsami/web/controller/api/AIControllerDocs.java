package com.balsamic.sejongmalsami.web.controller.api;

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
      AI 기반 유사 콘텐츠 검색을 통해 관련성 높은 질문 및 자료를 찾습니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - text (필수): 검색에 사용할 텍스트
      - threshold (선택): 유사도 임계값 (기본값: 0.8)
        * 0.95: 거의 동일한 내용
        * 0.90: 매우 유사한 내용
        * 0.85: 유사한 내용
        * 0.80: 관련성 있는 내용
        * 0.75 이하: 느슨한 관련성
      - contentType (선택): 검색할 콘텐츠 유형
        * DOCUMENT: 자료 게시글
        * QUESTION: 질문 게시글
      - pageNumber (선택): 페이지 번호 (기본값: 0)
      - pageSize (선택): 페이지당 항목 수 (기본값: 10)
      
      **응답 데이터**
      - EmbeddingDto: 검색 결과
        * embeddings: 유사도 순으로 정렬된 콘텐츠 목록
        * totalElements: 총 검색 결과 수
        * similarity: 각 결과의 유사도 점수
      
      **예외 상황**
      - INVALID_CONTENT_TYPE (400): 잘못된 콘텐츠 타입
      - EMBEDDING_GENERATION_FAILED (500): Embedding 생성 실패
      
      **참고사항**
      - Question 검색: threshold 0.7~0.8 권장 (다양한 결과)
      - Document 검색: threshold 0.8~0.9 권장 (정확성 우선)
      - threshold 값은 필요에 따라 조정하며 테스트 권장
      """
  )
  ResponseEntity<EmbeddingDto> searchSimilarEmbeddings(
      @ModelAttribute EmbeddingCommand command,
      @AuthenticationPrincipal CustomUserDetails customUserDetails);
}
