package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.postgres.NoticePost;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class NoticePostDto {

  private NoticePost noticePost;

  private List<NoticePost> noticePosts;
}
