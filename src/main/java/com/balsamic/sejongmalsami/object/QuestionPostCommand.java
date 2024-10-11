package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.QuestionPresetTag;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@Builder
@Getter
@Setter
@ToString
public class QuestionPostCommand {

  private UUID memberId;
  private String title;
  private String content;
  private String subject;
  private List<MultipartFile> mediaFile;
  private Set<QuestionPresetTag> questionPresetTagSet;
  private Set<String> customTagSet;
  private Integer rewardYeopjeon;
  private Boolean isPrivate;
}
