package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.YeopjeonAction;
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
  private String id;

  @Indexed
  private UUID memberId;            // 사용자 ID

  private Integer yeopjeon;         // 거래된 엽전 수 (양수: 획득, 음수: 소모)

  private YeopjeonAction yeopjeonAction;    // 엽전 거래 유형
}
