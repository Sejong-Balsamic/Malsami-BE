package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.postgres.NoticePost;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Page;

@Builder
@Getter
@ToString
public class NoticePostDto {

  private NoticePost noticePost;

  private List<NoticePost> noticePosts;

  private Page<NoticePost> noticePostsPage;
}
