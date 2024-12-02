package com.balsamic.sejongmalsami.object.postgres;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Faculty {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "uuid DEFAULT uuid_generate_v4()", updatable = false, nullable = false)
  private UUID facultyId;

  @Column(nullable = false, unique = true)
  @JsonProperty("faculty_name")
  private String facultyName;
}
