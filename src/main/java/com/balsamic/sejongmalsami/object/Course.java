package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.Faculty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@SuperBuilder
public class Course extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "course_id", columnDefinition = "uuid DEFAULT uuid_generate_v4()", updatable = false, nullable = false)
  private UUID courseId;

  private String subject; // 교과목명

  @Enumerated(EnumType.STRING)
  private Faculty faculty; // 단과대학

  private String department; // 학과
}
