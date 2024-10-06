package com.balsamic.sejongmalsami.object;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Builder
public class RefreshToken {
  @Id
  private String refreshTokenId;

  private String token;
  private UUID memberId;
  private LocalDateTime expiryDate;
}

