package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.ContentType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report extends BaseMongoEntity {

  @Id
  private String reportId;

  @Indexed
  @NotNull
  private UUID reporterId;      // 신고자 member ID

  private UUID reportedPostId;  // 신고 대상 게시글 ID

  private ContentType contentType;

  private String reason;          // 신고 사유
}
