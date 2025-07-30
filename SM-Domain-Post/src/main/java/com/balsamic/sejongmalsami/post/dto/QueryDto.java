package com.balsamic.sejongmalsami.post.dto;

import com.balsamic.sejongmalsami.mongo.SearchHistory;
import com.balsamic.sejongmalsami.postgres.DocumentPost;
import com.balsamic.sejongmalsami.postgres.DocumentRequestPost;
import com.balsamic.sejongmalsami.postgres.NoticePost;
import com.balsamic.sejongmalsami.postgres.QuestionPost;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Page;

@Builder
@Getter
@ToString
public class QueryDto {

  private Page<QuestionPost> questionPostsPage;

  private Page<DocumentPost> documentPostsPage;

  private Page<DocumentRequestPost> documentRequestPostsPage;

  private Page<NoticePost> noticePostsPage;

  private List<SearchHistory> searchHistoryList;
}
