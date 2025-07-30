package com.balsamic.sejongmalsami.application.dto;

import com.balsamic.sejongmalsami.constants.AccountStatus;
import com.balsamic.sejongmalsami.constants.ChaetaekStatus;
import com.balsamic.sejongmalsami.constants.QuestionPresetTag;
import com.balsamic.sejongmalsami.constants.Role;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
public class AdminCommand {

  public AdminCommand() {
    this.pageNumber = 0;
    this.pageSize = 30;
  }
  @Schema(hidden = true, description = "회원")
  @JsonIgnore
  private Member member;
  private Integer amount;
  private UUID targetMemberId;

  // 회원
  private Long studentId;
  private String studentName;
  private UUID memberId;
  private String memberIdStr;
  private String uuidNickname;
  private String major;
  private String academicYear;
  private String enrollmentStatus;
  private AccountStatus accountStatus;
  private Role role;
  private String lastLoginStart;
  private String lastLoginEnd;
  private Boolean isFirstLogin;
  private Boolean isEdited;
  private Boolean isDeleted;

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

  // TestService 관련 필드
  private Integer postCount;
  private boolean useMockMember;
  
  // AdminApiService 관련 필드 
  private String sejongPortalPassword;

  private Integer pageNumber;
  private Integer pageSize;
  private String sortField;
  private String sortDirection;
}
