package com.balsamic.sejongmalsami.object.mongo;

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
public class PurchaseHistory extends BaseMongoEntity {

  @Id
  private String purchaseHistoryId;

  @Indexed
  @NotNull
  private UUID memberId;

  @Indexed
  @NotNull
  private UUID documentPostId;

  @Indexed
  @NotNull
  private UUID DocumentFileId;

  @NotNull
  private Integer yeopjeonSpent; // 소모한 엽전양

  @NotNull
  private Integer resultYeopjeon;   // 남은 엽전양
}
