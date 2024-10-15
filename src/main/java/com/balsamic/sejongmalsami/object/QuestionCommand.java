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
public class QuestionCommand {

  private UUID memberId; // 질문, 답변
  private String title; // 질문
  private UUID questionPostId; // 답변
  private String content; // 질문, 답변
  private String subject; // 질문
  private List<MultipartFile> mediaFiles; // 질문, 답변
  private Set<QuestionPresetTag> questionPresetTagSet; // 질문
  private Set<String> customTagSet; // 질문
  private Integer rewardYeopjeon; // 질문
  private Boolean isChaetaek; // 답변
  private Boolean isPrivate; // 질문, 답변
}
