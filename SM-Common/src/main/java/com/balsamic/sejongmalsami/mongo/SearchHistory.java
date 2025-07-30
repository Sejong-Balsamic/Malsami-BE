package com.balsamic.sejongmalsami.mongo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistory extends BaseMongoEntity{

  @Id
  private String searchHistoryId;

  @Indexed(unique = true)
  private String keyword;

  @Builder.Default
  private Long searchCount = 0L; // 검색 횟수

  @Builder.Default
  private Integer lastRank = -1; // 이전 순위 (없는경우 -1)

  @Builder.Default
  private Integer currentRank = -1; // 현재 순위 (없는 경우 -1)

  @Builder.Default
  private Integer rankChange = 0; // 등락폭 (양수: 상숭, 음수: 하락)

  @Builder.Default
  private Boolean isNew = false; // 새롭게 10위권에 진입한 검색어 여부

  public void increaseSearchCount() {
    this.searchCount += 1;
  }
}
