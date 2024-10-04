package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.ExtensionType;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
public class MediaFileCommand {

  private UUID questionPostId;
  private UUID answerPostId;
  private String fileName;
  private String filePath;
  private ExtensionType fileType;
}
