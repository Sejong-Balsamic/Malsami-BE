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

  MISSING_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "쿠키에서 리프레시 토큰을 찾을 수 없습니다."),

  INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않거나 만료되었습니다."),

  EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "액세스 토큰이 만료되었습니다."),

  INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."),

  MISSING_AUTH_TOKEN(HttpStatus.UNAUTHORIZED, "인증 토큰이 필요합니다."),

  // File Uploads

  FILE_UPLOAD_ERROR(HttpStatus.BAD_REQUEST, "파일 업로드 시 오류가 발생했습니다."),

  INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다."),

  DUPLICATE_COURSE_UPLOAD(HttpStatus.BAD_REQUEST, "중복된 교과목명 파일입니다."),

  WRONG_COURSE_FILE_FORMAT(HttpStatus.BAD_REQUEST, "교과목명 업로드 파일 포맷 오류"),

  // Course

  COURSE_SAVE_ERROR(HttpStatus.BAD_REQUEST, "교과목명 파일 처리 중 오류가 발생했습니다"),

  WRONG_FACULTY_NAME(HttpStatus.BAD_REQUEST, "올바르지 않은 단과 대학입니다"),

  // Member

  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원이 존재하지 않습니다."),

  // QuestionPost

  QUESTION_POST_NOT_FOUND(HttpStatus.BAD_REQUEST, "질문 글을 찾을 수 없습니다."),

  QUESTION_TITLE_NULL(HttpStatus.BAD_REQUEST, "질문 게시글의 제목이 비어 있습니다."),

  QUESTION_CONTENT_NULL(HttpStatus.BAD_REQUEST, "질문 게시글의 본문이 비어 있습니다."),

  QUESTION_SUBJECT_NULL(HttpStatus.BAD_REQUEST, "과목이 설정되지 않았습니다."),

  QUESTION_PRESET_TAG_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "질문게시글 정적태그는 최대 2개까지 선택가능합니다."),

  QUESTION_REWARD_INVALID(HttpStatus.BAD_REQUEST, "질문게시글 엽전 현상금에 잘못된 값이 할당되었습니다."),

  // AnswerPost

  ANSWER_POST_NOT_FOUND(HttpStatus.BAD_REQUEST, "답변 글을 찾을 수 없습니다."),

  // DocumentPost

  DOCUMENT_TYPE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "자료게시글 자료 종류는 최대 2개까지 선택가능합니다."),

  // MediaFile

  MEDIA_FILE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "첨부할 수 있는 미디어 파일 개수를 초과했습니다."),

  // CustomTag

  CUSTOM_TAG_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "커스텀태그는 최대 4개까지 추가할 수 있습니다."),

  CUSTOM_TAG_LENGTH_EXCEEDED(HttpStatus.BAD_REQUEST, "커스텀태그 길이는 최대 10자 입니다.");

  private final HttpStatus status;
  private final String message;
}
