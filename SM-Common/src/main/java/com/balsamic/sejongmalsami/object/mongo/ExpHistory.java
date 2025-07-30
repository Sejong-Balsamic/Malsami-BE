package com.balsamic.sejongmalsami.object.mongo;

import com.balsamic.sejongmalsami.constants.ExpAction;
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
public class ExpHistory extends BaseMongoEntity {

  @Id
  private String expHistoryId;

  @Indexed
  @NotNull
  private UUID memberId; // 사용자 ID

  @NotNull
  private Integer expChange; // 변동된 경험치

  @NotNull
  private ExpAction expAction; // 경험치 변동 유형

  @NotNull
  private Integer resultExp; // 변동 이후 경험치
}
