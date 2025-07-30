package com.balsamic.sejongmalsami.application.dto;

import com.balsamic.sejongmalsami.notice.object.postgres.NoticePost;
import com.balsamic.sejongmalsami.object.mongo.SearchHistory;
import com.balsamic.sejongmalsami.post.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.post.object.postgres.DocumentRequestPost;
import com.balsamic.sejongmalsami.post.object.postgres.QuestionPost;
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
