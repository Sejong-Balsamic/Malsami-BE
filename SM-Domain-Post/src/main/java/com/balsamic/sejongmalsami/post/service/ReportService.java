package com.balsamic.sejongmalsami.post.service;

import com.balsamic.sejongmalsami.constants.ContentType;
import com.balsamic.sejongmalsami.object.ReportCommand;
import com.balsamic.sejongmalsami.object.ReportDto;
import com.balsamic.sejongmalsami.object.mongo.Report;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.post.object.postgres.AnswerPost;
import com.balsamic.sejongmalsami.post.object.postgres.Comment;
import com.balsamic.sejongmalsami.post.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.post.object.postgres.DocumentRequestPost;
import com.balsamic.sejongmalsami.post.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.post.repository.postgres.AnswerPostRepository;
import com.balsamic.sejongmalsami.post.repository.postgres.CommentRepository;
import com.balsamic.sejongmalsami.post.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.post.repository.postgres.DocumentRequestPostRepository;
import com.balsamic.sejongmalsami.post.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.repository.mongo.ReportRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportService {

  private final ReportRepository reportRepository;
  private final CommentRepository commentRepository;
  private final QuestionPostRepository questionPostRepository;
  private final AnswerPostRepository answerPostRepository;
  private final DocumentPostRepository documentPostRepository;
  private final DocumentRequestPostRepository documentRequestPostRepository;

  public ReportDto saveReportPost(ReportCommand command) {
    log.debug("신고 저장 시작: {}", command);

    // 신고자 정보 추출
    Member reporter = command.getMember();
    log.debug("신고자 정보 - memberId: {}, studentId: {}", reporter.getMemberId(), reporter.getStudentId());

    // 신고 대상의 ID와 타입 추출
    UUID reportedMemberId = null;
    UUID reportedId = command.getReportedEntityId();
    ContentType contentType = command.getContentType();

    log.debug("신고 대상 - contentType: {}, reportedId: {}", contentType, reportedId);

    // 신고 대상에 따라 분기 처리
    if (contentType.equals(ContentType.COMMENT)) {
      // 댓글 신고 처리
      Comment comment = commentRepository.findById(reportedId)
          .orElseThrow(() -> {
            log.error("해당 ID의 댓글을 찾을 수 없음: {}", reportedId);
            return new CustomException(ErrorCode.COMMENT_NOT_FOUND);
          });
      reportedMemberId = comment.getMember().getMemberId();
      log.debug("신고된 댓글의 작성자 memberId: {}", reportedMemberId);
    } else if (contentType.equals(ContentType.QUESTION)) {
      // 질문 게시글 신고 처리
      QuestionPost questionPost = questionPostRepository.findById(reportedId)
          .orElseThrow(() -> {
            log.error("해당 ID의 질문 게시글을 찾을 수 없음: {}", reportedId);
            return new CustomException(ErrorCode.QUESTION_POST_NOT_FOUND);
          });
      reportedMemberId = questionPost.getMember().getMemberId();
      log.debug("신고된 질문 게시글의 작성자 memberId: {}", reportedMemberId);
    } else if (contentType.equals(ContentType.ANSWER)) {
      // 답변 게시글 신고 처리
      AnswerPost answerPost = answerPostRepository.findById(reportedId)
          .orElseThrow(() -> {
            log.error("해당 ID의 답변 게시글을 찾을 수 없음: {}", reportedId);
            return new CustomException(ErrorCode.ANSWER_POST_NOT_FOUND);
          });
      reportedMemberId = answerPost.getMember().getMemberId();
      log.debug("신고된 답변 게시글의 작성자 memberId: {}", reportedMemberId);
    } else if (contentType.equals(ContentType.DOCUMENT)) {
      // 자료 게시글 신고 처리
      DocumentPost documentPost = documentPostRepository.findById(reportedId)
          .orElseThrow(() -> {
            log.error("해당 ID의 자료 게시글을 찾을 수 없음: {}", reportedId);
            return new CustomException(ErrorCode.DOCUMENT_POST_NOT_FOUND);
          });
      reportedMemberId = documentPost.getMember().getMemberId();
      log.debug("신고된 자료 게시글의 작성자 memberId: {}", reportedMemberId);
    } else if (contentType.equals(ContentType.DOCUMENT_REQUEST)) {
      // 자료 요청 게시글 신고 처리
      DocumentRequestPost documentRequestPost = documentRequestPostRepository.findById(reportedId)
          .orElseThrow(() -> {
            log.error("해당 ID의 자료 요청 게시글을 찾을 수 없음: {}", reportedId);
            return new CustomException(ErrorCode.DOCUMENT_REQUEST_POST_NOT_FOUND);
          });
      reportedMemberId = documentRequestPost.getMember().getMemberId();
      log.debug("신고된 자료 요청 게시글의 작성자 memberId: {}", reportedMemberId);
    } else {
      // 유효하지 않은 콘텐츠 타입 처리
      log.error("유효하지 않은 ContentType: {}", contentType);
      throw new CustomException(ErrorCode.INVALID_CONTENT_TYPE);
    }

    // 자신의 콘텐츠를 신고하려는 시도 방지
    if (reporter.getMemberId().equals(reportedMemberId)) {
      log.debug("신고자가 자신의 콘텐츠를 신고하려 함 - reporterId: {}, studentId: {}, reportedMemberId: {}",
          reporter.getMemberId(), reporter.getStudentId(), reportedMemberId);
      throw new CustomException(ErrorCode.CANNOT_REPORT_OWN_CONTENT);
    }

    // 중복 신고 방지 확인
    boolean isReportExists = reportRepository.existsByReporterIdAndReportedEntityId(
        reporter.getMemberId(), command.getReportedEntityId());
    log.error("중복 신고 여부 확인 - reporterId: {}, reportedEntityId: {}, exists: {}",
        reporter.getMemberId(), command.getReportedEntityId(), isReportExists);

    if (isReportExists) {
      log.error("이미 신고된 콘텐츠입니다 - reporterId: {}, reportedEntityId: {}",
          reporter.getMemberId(), command.getReportedEntityId());
      throw new CustomException(ErrorCode.ALREADY_REPORTED);
    }

    // 신고서 저장
    Report savedReport = reportRepository.save(
        Report.builder()
            .reporterId(reporter.getMemberId())
            .reportedMemberId(reportedMemberId)
            .reportedEntityId(command.getReportedEntityId())
            .contentType(command.getContentType())
            .reportReason(command.getReportReason())
            .build()
    );
    log.debug("신고 저장 완료 - reportId: {}", savedReport.getReportId());
    return ReportDto.builder()
        .report(savedReport)
        .build();
  }
}