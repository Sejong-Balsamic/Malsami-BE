package com.balsamic.sejongmalsami.util.init;

import com.balsamic.sejongmalsami.CommonUtil;
import com.balsamic.sejongmalsami.object.postgres.ServerErrorCode;
import com.balsamic.sejongmalsami.repository.postgres.ServerErrorCodeRepository;
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

  /**
   * ErrorCode enum을 DB에 초기화 또는 업데이트
   */
  @Transactional
  public void initErrorCodes() {
    String currentHash = calculateErrorCodeHash();

    // 기존에 저장된 HASH 엔트리 조회
    ServerErrorCode storedHashEntry = serverErrorCodeRepository.findByErrorCode("HASH").orElse(null);
    String storedHash = storedHashEntry != null ? storedHashEntry.getMessage() : null;

    if (!currentHash.equals(storedHash)) {
      // 기존 데이터 삭제
      serverErrorCodeRepository.deleteAll();

      // ErrorCode를 기반으로 데이터 생성
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

      // HASH 엔트리 저장
      ServerErrorCode hashEntry = ServerErrorCode.builder()
          .errorCode("HASH")
          .httpStatusCode(0) // 관리용 더미 값
          .httpStatusMessage("HASH")
          .message(currentHash)
          .build();
      serverErrorCodeRepository.save(hashEntry);
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
