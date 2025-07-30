package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.constants.ChaetaekStatus;
import com.balsamic.sejongmalsami.constants.QuestionPresetTag;
import com.balsamic.sejongmalsami.postgres.Member;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@ToString
public class AdminCommand {

  public AdminCommand() {
    this.pageNumber = 0;
    this.pageSize = 30;
  }

  private Member member;
  private Integer amount;
  private UUID targetMemberId;

  // 회원
  private Long studentId;
  private String studentName;
  private UUID memberId;
  private String memberIdStr;
  private String uuidNickname;

  // 과목
  private String subject;
  private String faculty;
  private Integer year;
  private Integer semester;

  // courseFile
  private MultipartFile multipartFile;
  private String fileName;

  // 에러코드
  private String errorCode;
  private Integer httpStatusCode;
  private String httpStatusMessage;
  private String message;

  // QuestionPost
  private String query;
  private List<QuestionPresetTag> questionPresetTags; // 정적 태그 검색(옵션)
  private ChaetaekStatus chaetaekStatus; // "ALL", "CHAETAEK", "NO_CHAETAEK"

  private Integer pageNumber;
  private Integer pageSize;
  private String sortField;
  private String sortDirection;
}
