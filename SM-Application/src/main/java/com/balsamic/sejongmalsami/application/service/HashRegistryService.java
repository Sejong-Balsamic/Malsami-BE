package com.balsamic.sejongmalsami.application.service;

import com.balsamic.sejongmalsami.constants.HashType;
import com.balsamic.sejongmalsami.object.postgres.HashRegistry;
import com.balsamic.sejongmalsami.repository.postgres.HashRegistryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HashRegistryService {

  private final HashRegistryRepository hashRegistryRepository;

  /**
   * 특정 HashType에 대한 현재 해시값을 조회합니다.
   *
   * @param hashType 해시값의 유형
   * @return 해시값 문자열 또는 null
   */
  @Transactional(readOnly = true)
  public String getHashValue(HashType hashType) {
    return hashRegistryRepository.findByHashType(hashType)
        .map(HashRegistry::getHashValue)
        .orElse(null);
  }

  /**
   * 특정 HashType에 대한 해시값을 업데이트하거나 생성합니다.
   *
   * @param hashType  해시값의 유형
   * @param hashValue 새로운 해시값
   */
  @Transactional
  public void updateHashValue(HashType hashType, String hashValue) {
    HashRegistry hashRegistry = hashRegistryRepository.findByHashType(hashType)
        .orElse(HashRegistry.builder()
            .hashType(hashType)
            .hashValue(hashValue)
            .message(null)
            .build());
    hashRegistry.setHashValue(hashValue);
    hashRegistryRepository.save(hashRegistry);
  }
}
