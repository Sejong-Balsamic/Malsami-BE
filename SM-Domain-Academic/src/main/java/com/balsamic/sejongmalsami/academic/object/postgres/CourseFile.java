package com.balsamic.sejongmalsami.academic.object.postgres;

import com.balsamic.sejongmalsami.object.postgres.BaseEntity;
import com.balsamic.sejongmalsami.constants.FileStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
@SuperBuilder
public class CourseFile extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "uuid DEFAULT uuid_generate_v4()", updatable = false, nullable = false)
  private UUID courseFileId;

  @Column(unique = true)
  private String fileName;

  private Integer year;

  private Integer semester;

  private LocalDateTime processedAt;

  @Enumerated(EnumType.STRING)
  private FileStatus fileStatus;

  private String filePath;

  private String errorMessage;

  private Long durationSeconds;
}
