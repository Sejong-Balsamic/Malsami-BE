package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.ExtensionType;
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
public class MediaFileCommand {

  private UUID questionPostId;
  private UUID answerPostId;
  private MultipartFile file;
  private String filePath;
  private Long fileSize;
  private ExtensionType fileType;
}
