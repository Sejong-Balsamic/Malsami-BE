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
public class DocumentCommand {

  private UUID memberId; // 자료, 자료 요청
  private String title; // 자료, 자료 요청
  private String content; // 자료, 자료 요청
  private String subject; // 자료, 자료 요청
  private Set<DocumentType> documentTypeSet; // 자료, 자료 요청
  private Boolean isDepartmentPrivate; // 자료
  private Boolean isPrivate; // 자료 요청
}
