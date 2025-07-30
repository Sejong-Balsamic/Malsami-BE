package com.balsamic.sejongmalsami.object.postgres;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
// 프론트 공유 에러코드
public class ServerErrorCode {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "uuid DEFAULT uuid_generate_v4()", updatable = false, nullable = false)
  private UUID serverErrorCodeId;

  @Column(unique = true)
  private String errorCode; // ErrorCode.name()

  @Column(nullable = false)
  private int httpStatusCode; // 403

  @Column(nullable = false)
  private String httpStatusMessage;

  @Column(nullable = false)
  private String message; // 에러 메시지
}
