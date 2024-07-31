package com.balsamic.sejongmalsami.common.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SejongStudentAuthResponse {
  private String major;

  private String studentIdString;

  private String studentName;

  private String academicYear;

  private String enrollmentStatus;
}
