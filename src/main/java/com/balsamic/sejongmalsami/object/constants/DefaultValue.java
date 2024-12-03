package com.balsamic.sejongmalsami.object.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DefaultValue {
  UNDEFINED("정의되지 않음"),           // 값이 설정되지 않았을 때
  EMPTY("비어 있음"),                  // 값이 있지만 내용이 없는 경우
  NOT_APPLICABLE("해당 없음"),          // 특정 상황에서 의미가 없을 때
  UNKNOWN("알 수 없음"),               // 데이터가 있지만 확인할 수 없을 때
  DEFAULT("기본값"),                   // 기본적으로 설정된 값
  NOT_FOUND("찾을 수 없음"),            // 데이터를 찾을 수 없을 때
  INVALID("유효하지 않음"),             // 값이 잘못되었을 때
  DEPRECATED("더 이상 사용되지 않음"),   // 오래된 값일 때
  INITIAL("초기 상태"),                // 초기값을 나타낼 때
  PENDING("대기 중"),                  // 처리 중이거나 보류 상태일 때
  IN_PROGRESS("진행 중"),              // 작업이 진행 중일 때
  COMPLETED("완료됨"),                 // 작업이 완료되었을 때
  FAILED("실패"),                     // 작업이 실패했을 때
  SUCCESS("성공"),                    // 작업이 성공했을 때
  NONE("없음"),                       // 값이 없음을 명시적으로 나타냄
  TEMPORARY("임시 값"),                // 임시로 설정된 값
  SYSTEM_DEFAULT("시스템 기본값"),      // 시스템에서 자동으로 설정한 값
  UNKNOWN_USER("알 수 없는 사용자"),    // 사용자 데이터를 확인할 수 없을 때
  UNVERIFIED("검증되지 않음");         // 데이터가 검증되지 않았을 때

  private final String description;
}

