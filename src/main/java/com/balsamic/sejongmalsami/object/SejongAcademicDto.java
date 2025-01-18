package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.postgres.Faculty;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SejongAcademicDto implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  @Builder.Default
  private List<Faculty> faculties = new ArrayList<>();

  @Builder.Default
  private List<String> subjects = new ArrayList<>();
}
