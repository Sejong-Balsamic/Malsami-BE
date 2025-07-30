package com.balsamic.sejongmalsami.repository.mongo;

import com.balsamic.sejongmalsami.mongo.RefreshToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
  Optional<RefreshToken> findByToken(String token);
  Optional<RefreshToken> findByMemberId(UUID memberId);
  void deleteByToken(String token);
}