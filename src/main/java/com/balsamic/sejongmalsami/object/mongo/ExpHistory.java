package com.balsamic.sejongmalsami.object.mongo;

import com.balsamic.sejongmalsami.object.constants.ActionType;
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
  private UUID memberId;

  @NotNull
  private Integer expChange;

  @NotNull
  private ActionType actionType;
}
