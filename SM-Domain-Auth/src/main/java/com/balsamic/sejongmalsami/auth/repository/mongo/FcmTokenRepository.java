package com.balsamic.sejongmalsami.auth.repository.mongo;

import com.balsamic.sejongmalsami.auth.object.mongo.FcmToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FcmTokenRepository extends MongoRepository<FcmToken, String> {

  Optional<FcmToken> findByFcmToken(String fcmToken);

  Boolean existsByFcmToken(String fcmToken);

  Optional<FcmToken> findByMemberId(UUID memberId);

  void deleteByMemberIdAndFcmToken(UUID memberId, String fcmToken);
}
