package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.object.postgres.DocumentRequestPost;
import com.balsamic.sejongmalsami.object.postgres.NoticePost;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
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
}
