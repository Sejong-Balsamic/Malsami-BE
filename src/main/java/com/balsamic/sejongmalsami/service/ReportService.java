package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.ReportCommand;
import com.balsamic.sejongmalsami.object.ReportDto;
import com.balsamic.sejongmalsami.object.mongo.Report;
import com.balsamic.sejongmalsami.repository.mongo.ReportRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {

  private final ReportRepository reportRepository;

  public ReportDto saveReportPost(ReportCommand command) {

    // 중복 신고 방지
    boolean isReportExists = reportRepository.existsByReporterIdAndReportedIdAndReportedEntityId(
        command.getMemberId(), command.getReportedId(), command.getReportedEntityId());
    if (isReportExists) {
      throw new CustomException(ErrorCode.ALREADY_REPORTED);
    }

    // 보고서 저장
    Report savedReport = reportRepository.save(
        Report.builder()
            .reporterId(command.getMemberId())
            .reportedId(command.getReportedId())
            .reportedEntityId(command.getReportedEntityId())
            .contentType(command.getContentType())
            .reportReason(command.getReportReason())
            .message(command.getMessage())
            .build()
    );

    return ReportDto.builder()
        .report(savedReport)
        .build();
  }
}
