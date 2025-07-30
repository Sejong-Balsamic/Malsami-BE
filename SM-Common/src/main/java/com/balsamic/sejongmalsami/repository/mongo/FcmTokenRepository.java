package com.balsamic.sejongmalsami.repository.mongo;

import com.balsamic.sejongmalsami.mongo.FcmToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FcmTokenRepository extends MongoRepository<FcmToken, String> {

  Optional<FcmToken> findByFcmToken(String fcmToken);

  Boolean existsByFcmToken(String fcmToken);

  Optional<FcmToken> findByMemberId(UUID memberId);

  void deleteByMemberIdAndFcmToken(UUID memberId, String fcmToken);
}
