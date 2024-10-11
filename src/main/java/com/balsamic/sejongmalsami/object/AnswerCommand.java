package com.balsamic.sejongmalsami.object;

import java.util.List;
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
public class AnswerCommand {

  private UUID memberId;
  private UUID questionPostId;
  private String content;
  private List<MultipartFile> mediaFiles;
  private Boolean isChaetaek;
}
