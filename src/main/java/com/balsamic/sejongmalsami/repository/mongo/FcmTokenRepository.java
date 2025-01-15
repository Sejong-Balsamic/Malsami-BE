package com.balsamic.sejongmalsami.repository.mongo;

import com.balsamic.sejongmalsami.object.mongo.FcmToken;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FcmTokenRepository extends MongoRepository<FcmToken, String> {

  void deleteByMemberIdAndToken(UUID memberId, String token);
}
