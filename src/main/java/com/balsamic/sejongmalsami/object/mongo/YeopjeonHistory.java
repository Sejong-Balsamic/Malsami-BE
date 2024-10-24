package com.balsamic.sejongmalsami.object.mongo;

import com.balsamic.sejongmalsami.object.constants.YeopjeonAction;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.UUID;

@Document
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YeopjeonHistory extends BaseMongoEntity {

  @Id
  private String yeopjeonHistoryId;

  @Indexed
  @NotNull
  private UUID memberId;            // 사용자 ID
  
  private Integer yeopjeonChange;         // 거래된 엽전 수 (양수: 획득, 음수: 소모)

  private YeopjeonAction yeopjeonAction;    // 엽전 거래 유형

  private Integer resultYeopjeon;   // 거래 이후 엽전량
}
