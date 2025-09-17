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
      부적절한 게시글, 댓글 등을 신고하여 커뮤니티 건전성을 유지합니다.
      
      **인증 요구사항**
      - 인증 필요: 있음
      - 권한: USER
      
      **요청 파라미터**
      - reportedEntityId (필수): 신고 대상 게시글/댓글 ID
      - contentType (필수): 신고 대상 콘텐츠 타입
        * COMMENT: 댓글
        * QUESTION: 질문글
        * ANSWER: 답변글
        * DOCUMENT: 자료글
        * DOCUMENT_REQUEST: 자료요청글
      - reportReason (필수): 신고 사유
        * INAPPROPRIATE_BOARD: 게시판 성격에 부적절
        * PROFANITY: 욕설이나 비하 표현
        * OBSCENE_INTERACTION: 음란물이나 불건전한 만남 유도
        * ADVERTISEMENT: 광고, 판매글
        * FRAUD_IMPERSONATION: 유출, 사칭, 사기 의심
        * SPAM: 낚시, 놀람, 도배
        * INFO_REQUEST: 개인정보 요구
        * OFF_TOPIC: 풀이와 상관없는 대화
        * CASH_REQUEST: 금전적 보상 요구
        * INAPPROPRIATE_PHOTOS: 부적절한 사진 포함
        * COPYRIGHT_VIOLATION: 저작권 침해
        * OTHER: 기타
      
      **응답 데이터**
      - ReportDto: 신고 접수 정보
        * report: 저장된 신고 객체
        * reportId: 신고 접수 번호
      
      **예외 상황**
      - ALREADY_REPORTED (409): 이미 신고한 게시글
      - CANNOT_REPORT_OWN_CONTENT (403): 본인 게시물 신고 불가
      - CONTENT_NOT_FOUND (404): 신고 대상 콘텐츠가 존재하지 않음
      - MEMBER_NOT_FOUND (404): 신고자 정보를 찾을 수 없음
      - INVALID_CONTENT_TYPE (400): 잘못된 콘텐츠 타입
      
      **참고사항**
      - 중복 신고는 불가능하며, 본인이 작성한 콘텐츠는 신고할 수 없음
      - 신고는 관리자 검토 후 조치되며, 허위 신고 시 제재 가능
      """
  )
  ResponseEntity<ReportDto> saveReportPost(
      @AuthenticationPrincipal CustomUserDetails customUserDetails,
      @ModelAttribute ReportCommand command
  );
}
