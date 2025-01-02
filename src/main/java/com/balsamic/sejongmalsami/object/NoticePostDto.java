package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.postgres.NoticePost;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Page;

@Builder
@Getter
@ToString
public class NoticePostDto {

  private NoticePost noticePost;

  private Page<NoticePost> noticePostsPage;
}
