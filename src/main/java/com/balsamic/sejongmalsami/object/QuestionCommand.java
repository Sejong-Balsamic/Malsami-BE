package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.constants.QuestionPresetTag;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class QuestionCommand {

  private UUID postId; // 질문, 답변
  private UUID memberId; // 질문, 답변
  private String title; // 질문
  private UUID questionPostId; // 답변
  private String content; // 질문, 답변
  private String subject; // 질문
  private List<MultipartFile> mediaFiles; // 질문, 답변
  private Set<QuestionPresetTag> questionPresetTagSet; // 질문
  private Set<String> customTagSet; // 질문
  private Integer rewardYeopjeon; // 질문
  private ContentType contentType;
  private Boolean isChaetaek; // 답변
  private Boolean isPrivate; // 질문, 답변
  private Integer pageNumber = 0; // n번째 페이지 조회
  private Integer pageSize = 30; // n개의 데이터 조회


}
