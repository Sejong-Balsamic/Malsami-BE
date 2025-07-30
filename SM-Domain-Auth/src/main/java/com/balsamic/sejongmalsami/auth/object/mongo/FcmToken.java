package com.balsamic.sejongmalsami.auth.object.mongo;

import com.balsamic.sejongmalsami.object.mongo.BaseMongoEntity;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FcmToken extends BaseMongoEntity {

  @Id
  private String fcmTokenId;

  @NotNull
  @Indexed
  private UUID memberId;

  @NotNull
  private String fcmToken;
}
