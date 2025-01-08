package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.ReportCommand;
import com.balsamic.sejongmalsami.object.ReportDto;
import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.mongo.Report;
import com.balsamic.sejongmalsami.object.postgres.*;
import com.balsamic.sejongmalsami.repository.mongo.ReportRepository;
import com.balsamic.sejongmalsami.repository.postgres.*;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {

  private final ReportRepository reportRepository;
  private final CommentRepository commentRepository;
  private final QuestionPostRepository questionPostRepository;
  private final AnswerPostRepository answerPostRepository;
  private final DocumentPostRepository documentPostRepository;
  private final DocumentRequestPostRepository documentRequestPostRepository;

  public ReportDto saveReportPost(ReportCommand command) {

    // 신고자
    Member reporter = command.getMember();

    // 신고한 Entity 유효성 확인 및 신고 당한자 조회
    UUID reportedMemberId = null;
    UUID reportedId = command.getReportedEntityId();
    ContentType contentType = command.getContentType();

    // 댓글
    if(contentType.equals(ContentType.COMMENT)) {
      Comment comment = commentRepository.findById(reportedId)
          .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
      reportedMemberId = comment.getMember().getMemberId();
    // 질문
    } else if (contentType.equals(ContentType.QUESTION)) {
      QuestionPost questionPost = questionPostRepository.findById(reportedId)
          .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_POST_NOT_FOUND));
      reportedMemberId = questionPost.getMember().getMemberId();
    // 답변
    } else if (contentType.equals(ContentType.ANSWER)) {
      AnswerPost answerPost = answerPostRepository.findById(reportedId)
          .orElseThrow(() -> new CustomException(ErrorCode.ANSWER_POST_NOT_FOUND));
      reportedMemberId = answerPost.getMember().getMemberId();
    // 자료
    } else if (contentType.equals(ContentType.DOCUMENT)) {
      DocumentPost documentPost = documentPostRepository.findById(reportedId)
          .orElseThrow(() -> new CustomException(ErrorCode.DOCUMENT_POST_NOT_FOUND));
      reportedMemberId = documentPost.getMember().getMemberId();
    // 자료 요청
    } else if (contentType.equals(ContentType.DOCUMENT_REQUEST)) {
      DocumentRequestPost documentRequestPost = documentRequestPostRepository.findById(reportedId)
          .orElseThrow(() -> new CustomException(ErrorCode.DOCUMENT_REQUEST_POST_NOT_FOUND));
      reportedMemberId = documentRequestPost.getMember().getMemberId();
    }

    // 자신의 자료글 신고 방지
    if (reporter.getMemberId().equals(reportedMemberId)) {
      throw new CustomException(ErrorCode.CANNOT_REPORT_OWN_CONTENT);
    }

    // 중복 신고 방지
    boolean isReportExists = reportRepository.existsByReporterIdAndReportedEntityId(
        reporter.getMemberId(), command.getReportedEntityId());
    if (isReportExists) {
      throw new CustomException(ErrorCode.ALREADY_REPORTED);
    }

    // 보고서 저장
    Report savedReport = reportRepository.save(
        Report.builder()
            .reporterId(reporter.getMemberId())
            .reportedMemberId(reportedMemberId)
            .reportedEntityId(command.getReportedEntityId())
            .contentType(command.getContentType())
            .reportReason(command.getReportReason())
            .build()
    );

    return ReportDto.builder()
        .report(savedReport)
        .build();
  }
}