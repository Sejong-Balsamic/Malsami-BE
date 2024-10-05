package com.balsamic.sejongmalsami.object;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseHistory extends BaseMongoEntity {

  @Id
  private String purchaseHistoryId;

  @NotNull
  @Indexed
  private UUID memberId;

  @Indexed
  @NotNull
  private UUID documentPostId;

  @Indexed
  @NotNull
  private UUID mediaFileId;

  @NotNull
  private Integer yeopjeonQuantity; // 소모한 엽전양
}
