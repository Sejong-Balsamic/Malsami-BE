package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.PostType;
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

  private UUID questionId;
  private MultipartFile file;
  private PostType postType;
}
