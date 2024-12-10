package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.postgres.Faculty;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
@Builder
public class SejongAcademicDto {
  private List<Faculty> faculties;
}
