package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.AccountStatus;
import com.balsamic.sejongmalsami.object.constants.Role;
import com.balsamic.sejongmalsami.object.postgres.Member;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MemberCommand {

  private UUID memberId;
  private Long studentId;
  private String studentIdString;
  private String studentName;
  private String uuidNickname;
  private String major;
  private String academicYear;
  private String enrollmentStatus;

  private Member member;

  // auth
  private String sejongPortalId;
  private String sejongPortalPassword;

  // Tabulator
  // 페이징 관련 필드 추가
  private Integer pageNumber;        // 현재 페이지 (0부터 시작)
  private Integer pageSize;        // 페이지 크기
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
