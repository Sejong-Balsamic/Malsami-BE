package com.balsamic.sejongmalsami.util.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
  // Global

  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 문제가 발생했습니다."),

  INVALID_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

  ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근이 거부되었습니다."),

  // Auth

  SEJONG_AUTH_SESSION_FAILURE(HttpStatus.FORBIDDEN, "세종대학교 로그인 세션 ID를 가져오는 데 실패했습니다."),

  SEJONG_AUTH_CREDENTIALS_INVALID(HttpStatus.FORBIDDEN, "세종대학교 로그인에 실패했습니다: 잘못된 자격 증명입니다."),

  SEJONG_AUTH_CONNECTION_FAILURE(HttpStatus.FORBIDDEN, "세종대학교 로그인 페이지에 연결할 수 없습니다."),

  SEJONG_AUTH_DATA_FETCH_FAILURE(HttpStatus.FORBIDDEN, "세종대학교 학생 데이터를 가져오는 데 실패했습니다."),

  // File Uploads

  INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "파일 형식이 잘못되었습니다."),

  DUPLICATE_COURSE_UPLOAD(HttpStatus.BAD_REQUEST, "중복된 교과목명 파일입니다."),

  WRONG_COURSE_FILE_FORMAT(HttpStatus.BAD_REQUEST, "교과목명 업로드 파일 포맷 오류"),

  // Course

  COURSE_SAVE_ERROR(HttpStatus.BAD_REQUEST, "교과목명 파일 처리 중 오류가 발생했습니다"),

  WRONG_FACULTY_NAME(HttpStatus.BAD_REQUEST, "올바르지 않은 단과 대학입니다"),

  // Member

  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원이 존재하지 않습니다.");

  private final HttpStatus status;
  private final String message;
}