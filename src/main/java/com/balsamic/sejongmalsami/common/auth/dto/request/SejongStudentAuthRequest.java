package com.balsamic.sejongmalsami.common.auth.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SejongStudentAuthRequest {
  private String sejongPortalId;
  private String sejongPortalPassword;
}
