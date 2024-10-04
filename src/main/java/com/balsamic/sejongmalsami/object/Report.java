package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.ReportType;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report extends BaseMongoEntity {

  @Id
  private String reportId;

  private UUID reporterId;      // 신고자 member ID

  private String reportedPostId;  // 신고 대상 게시글 ID

  private ReportType reportType;  // 신고 유형 (QUESTION_POST, DOCUMENT_POST, DOCUMENT_REQUEST_POST, ANSWER_POST)

  private String reason;          // 신고 사유
}
