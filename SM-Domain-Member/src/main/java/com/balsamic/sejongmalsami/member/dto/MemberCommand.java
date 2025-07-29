package com.balsamic.sejongmalsami.member.dto;

import com.balsamic.sejongmalsami.object.constants.AccountStatus;
import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.constants.Role;
import com.balsamic.sejongmalsami.object.constants.SortType;
import com.balsamic.sejongmalsami.object.postgres.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
public class MemberCommand {

  public MemberCommand() {
    this.pageNumber = 0;
    this.pageSize = 30;
  }

  private UUID memberId;
  private String memberIdStr;
  private Long studentId;
  private String studentIdString;
  private String studentName;
  private String uuidNickname;
  private String major;
  private String academicYear;
  private String enrollmentStatus;
  private String faculty;

  private Member member;

  // auth
  private String sejongPortalId;
  private String sejongPortalPassword;

  // Tabulator
  // 페이징 관련 필드 추가
  @Schema(defaultValue = "0")
  private Integer pageNumber;        // 현재 페이지 (0부터 시작)
  @Schema(defaultValue = "30")
  private Integer pageSize;        // 페이지 크기
  private ContentType contentType; // 작성 글 종류
  private SortType sortType; // 정렬 조건
  private String sortField;    // 정렬 필드
  private String sortDirection; // 정렬 방향 (asc/desc)

  // 검색 필터 관련 필드
  private String searchTerm;    // 통합 검색어
  private AccountStatus accountStatus; // 계정 상태 필터
  private Role role;         // 역할 필터
  private String lastLoginStart;
  private String lastLoginEnd;
  private Boolean isFirstLogin;
  private Boolean isEdited;
  private Boolean isDeleted;
}
