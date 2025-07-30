package com.balsamic.sejongmalsami.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Builder
@Getter
@Setter
@ToString
public class TestDto {

  private String fileUrl;
  private int createdPostCount;
  private String timeTaken;
}
