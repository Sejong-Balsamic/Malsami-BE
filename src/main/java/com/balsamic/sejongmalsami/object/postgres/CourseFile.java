package com.balsamic.sejongmalsami.object.postgres;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@ToString
@SuperBuilder
public class CourseFile extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "uuid DEFAULT uuid_generate_v4()", updatable = false, nullable = false)
  private UUID courseFileId;

  private String fileName;

  private Integer year;

  private Integer semester;

  private LocalDateTime processedAt;

  private Boolean success;

  private String errorMessage;
}
