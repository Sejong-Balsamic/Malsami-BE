package com.balsamic.sejongmalsami.auth.object.mongo;

import com.balsamic.sejongmalsami.object.mongo.BaseMongoEntity;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Builder
public class RefreshToken {
  @Id
  private String refreshTokenId;

  @NotNull
  private String token;

  @Indexed
  @NotNull
  private UUID memberId;

  @NotNull
  private LocalDateTime expiryDate;
}

