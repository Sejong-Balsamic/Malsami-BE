package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.DocumentType;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@Builder
public class DocumentPostCommand {

  private UUID memberId;
  private String title;
  private String content;
  private String subject;
  private Set<DocumentType> documentTypeSet;
  private Boolean isDepartmentPrivate;
}
