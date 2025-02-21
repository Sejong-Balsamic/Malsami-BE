package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.mongo.SearchHistory;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class SearchHistoryDto {

  private List<SearchHistory> searchHistoryList;
}
