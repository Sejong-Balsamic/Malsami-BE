package com.balsamic.sejongmalsami.util.init;

import com.balsamic.sejongmalsami.constants.HashType;
import com.balsamic.sejongmalsami.postgres.ServerErrorCode;
import com.balsamic.sejongmalsami.repository.postgres.ServerErrorCodeRepository;
import com.balsamic.sejongmalsami.service.HashRegistryService;
import com.balsamic.sejongmalsami.util.CommonUtil;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServerErrorCodeService {

  private final ServerErrorCodeRepository serverErrorCodeRepository;
  private final HashRegistryService hashRegistryService;

  /**
   * ErrorCode enum을 DB에 초기화 또는 업데이트
   */
  @Transactional
  public void initErrorCodes() {
    String currentHash = calculateErrorCodeHash();

    // 기존 HASH 조회 및 확인
    String storedHash = hashRegistryService.getHashValue(HashType.SERVER_ERROR_CODES);

    // HASH 값이 기존과 다른 경우 ( ServerErrorCode 초기화 )
    if (!currentHash.equals(storedHash)) {

      // 기존 데이터 삭제
      serverErrorCodeRepository.deleteAll();
      serverErrorCodeRepository.flush(); // Hibernate 캐시 강제 DB 반영

      // ServerErrorCode 생성 및 저장
      Arrays.stream(ErrorCode.values()).forEach(errorCode -> {
        HttpStatus status = errorCode.getStatus();
        ServerErrorCode serverErrorCode = ServerErrorCode.builder()
            .errorCode(errorCode.name())
            .httpStatusCode(status.value())
            .httpStatusMessage(status.getReasonPhrase())
            .message(errorCode.getMessage())
            .build();
        serverErrorCodeRepository.save(serverErrorCode);
      });

      // HASH 저장
      hashRegistryService.updateHashValue(HashType.SERVER_ERROR_CODES, currentHash);
    }
  }

  // 에러코드 해시값 계산
  public String calculateErrorCodeHash() {
    List<String> errorCodeStrings = Arrays.stream(ErrorCode.values())
        .map(ec -> ec.name() + "|" + ec.getStatus().value() + "|" + ec.getStatus().getReasonPhrase() + "|" + ec.getMessage())
        .collect(Collectors.toList());

    String concatenated = String.join(";", errorCodeStrings);
    return CommonUtil.calculateSha256ByStr(concatenated);
  }
}
