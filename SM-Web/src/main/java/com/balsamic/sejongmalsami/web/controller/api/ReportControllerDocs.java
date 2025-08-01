package com.balsamic.sejongmalsami.web.controller.api;

import com.balsamic.sejongmalsami.auth.dto.CustomUserDetails;
import com.balsamic.sejongmalsami.constants.Author;
import com.balsamic.sejongmalsami.post.dto.ReportCommand;
import com.balsamic.sejongmalsami.post.dto.ReportDto;
import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;

public interface ReportControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2025.01.08",
          author = Author.SUHSAECHAN,
          description = "ContentType 에 대한 Entity 유효성 확인, Member 유효성 확인 중복신고, 셀프신고 예외처리"
      ),
      @ApiChangeLog(
          date = "2025.01.05",
          author = Author.SUHSAECHAN,
          description = "신고 저장 | TODO: ContentType 에 대한 Entity 존재 확인"
      )
  })
  @Operation(
      summary = "게시글 신고",
      description = """
            **게시글 신고 기능**
            
            사용자가 특정 게시글을 신고할 수 있는 기능을 제공합니다.
            
            **이 API는 인증이 필요하며, JWT 토큰이 존재해야 합니다**
            
            ### **요청 파라미터**
            - **`ReportCommand`**: 신고에 필요한 정보
              - **`UUID reportedEntityId`**: 신고 대상 게시글 ID
              - **`ContentType contentType`**: 신고 대상 콘텐츠 타입 (예: QUESTION, ANSWER 등)
              - **`ReportReason reportReason`**: 신고 사유 (예: SPAM, HARASSMENT 등)
              
            ### **ContentType**
            - **`COMMENT`**: 댓글
            - **`QUESTION`**: 질문글
            - **`ANSWER`**: 답변글
            - **`DOCUMENT`**: 자료글
            - **`DOCUMENT_REQUEST`**: 자료요청글            
            
            ### **ReportReason 설명**
            - **`INAPPROPRIATE_BOARD`**: 게시판 성격에 부적절해요
            - **`PROFANITY`**: 욕설이나 비하가 있어요
            - **`OBSCENE_INTERACTION`**: 음란물이나 불건전한 만남 및 대화를 유도해요
            - **`ADVERTISEMENT`**: 광고, 판매글이에요
            - **`FRAUD_IMPERSONATION`**: 유출, 사칭, 사기가 의심돼요
            - **`SPAM`**: 낚시, 놀람, 도배가 있어요
            - **`INFO_REQUEST`**: 개인정보, SNS 등의 정보를 요구해요
            - **`OFF_TOPIC`**: 풀이와 상관없는 대화를 시도해요
            - **`CASH_REQUEST`**: 금전적인 보상, 현금을 요구해요
            - **`INAPPROPRIATE_PHOTOS`**: 부적절한 내용과 사진이 포함됐어요
            - **`COPYRIGHT_VIOLATION`**: 저작권 침해 또는 무단 도용이에요
            - **`OTHER`**: 기타
            
            ### **반환 파라미터 값**
            - **`ReportDto`**: 저장된 신고 정보
              - **`Report report`**: 저장된 신고 객체
              
            ### **오류 코드**
            - **`ALREADY_REPORTED`**: 이미 해당 게시글을 신고한 경우
            - **`CANNOT_REPORT_OWN_CONTENT`**: 자신의 게시물을 신고하려는 경우
            - **`COMMENT_NOT_FOUND`**: 신고하려는 댓글을 찾을 수 없는 경우
            - **`QUESTION_POST_NOT_FOUND`**: 신고하려는 질문 게시글을 찾을 수 없는 경우
            - **`ANSWER_POST_NOT_FOUND`**: 신고하려는 답변 게시글을 찾을 수 없는 경우
            - **`DOCUMENT_POST_NOT_FOUND`**: 신고하려는 자료 게시글을 찾을 수 없는 경우
            - **`DOCUMENT_REQUEST_POST_NOT_FOUND`**: 신고하려는 자료요청 게시글을 찾을 수 없는 경우
            - **`MEMBER_NOT_FOUND`**: 신고자 또는 신고 대상자 정보를 찾을 수 없는 경우
            - **`INVALID_CONTENT_TYPE`**: 잘못된 콘텐츠 타입이 지정된 경우
            - **`INTERNAL_SERVER_ERROR`**: 서버 내부 오류 발생 시
            """
  )
  ResponseEntity<ReportDto> saveReportPost(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute ReportCommand command
  );
}
