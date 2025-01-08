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

  SEJONG_AUTH_SESSION_ERROR(HttpStatus.FORBIDDEN, "세종대학교 로그인 세션 ID를 가져오는 데 실패했습니다."),

  SEJONG_AUTH_CREDENTIALS_INVALID(HttpStatus.FORBIDDEN, "세종대학교 로그인에 실패했습니다: 잘못된 자격 증명입니다."),

  SEJONG_AUTH_CONNECTION_ERROR(HttpStatus.FORBIDDEN, "세종대학교 로그인 페이지에 연결할 수 없습니다."),

  SEJONG_AUTH_DATA_FETCH_ERROR(HttpStatus.FORBIDDEN, "세종대학교 학생 데이터를 가져오는 데 실패했습니다."),

  TEST_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "테스트 회원을 찾을 수 없습니다"),

  TEST_MEMBER_AUTH_CREDENTIALS_INVALID(HttpStatus.FORBIDDEN, "테스트 회원 인증에 실패하였습니다"),

  MISSING_REFRESH_TOKEN(HttpStatus.FORBIDDEN, "쿠키에서 리프레시 토큰을 찾을 수 없습니다."),

  INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않거나 만료되었습니다."),

  EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "액세스 토큰이 만료되었습니다."),

  EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),

  INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."),

  MISSING_AUTH_TOKEN(HttpStatus.UNAUTHORIZED, "인증 토큰이 필요합니다."),

  // OpenAI API
  OPENAI_AUTHENTICATION_ERROR(HttpStatus.UNAUTHORIZED, "OpenAI 인증 실패: API 키가 잘못되었거나 만료되었습니다."),

  OPENAI_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "OpenAI 요청 한도를 초과했습니다."),

  OPENAI_INVALID_REQUEST_ERROR(HttpStatus.BAD_REQUEST, "OpenAI 요청이 유효하지 않습니다."),

  OPENAI_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "OpenAI 서버에서 문제가 발생했습니다."),

  OPENAI_TIMEOUT_ERROR(HttpStatus.REQUEST_TIMEOUT, "OpenAI 요청 시간이 초과되었습니다."),

  OPENAI_CONNECTION_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "OpenAI 서비스에 연결할 수 없습니다."),

  OPENAI_UNSUPPORTED_MODEL(HttpStatus.BAD_REQUEST, "OpenAI에서 지원하지 않는 모델 요청입니다."),

  OPENAI_INVALID_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "OpenAI로부터 올바르지 않은 응답을 받았습니다."),

  OPENAI_REQUEST_PAYLOAD_TOO_LARGE(HttpStatus.BAD_REQUEST, "OpenAI 요청 본문 크기가 너무 큽니다."),

  OPENAI_RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 OpenAI 리소스를 찾을 수 없습니다."),

  OPENAI_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "OpenAI 요청 권한이 없습니다."),

  // Embedding

  EMBEDDING_GENERATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Embedding 생성에 실패했습니다."),

  EMBEDDING_SEARCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "Embedding 검색에 실패했습니다."),

  // File

  S3_FILE_UPLOAD_ERROR(HttpStatus.BAD_REQUEST, "파일 업로드 시 오류가 발생했습니다."),

  S3_FILE_DELETE_ERROR(HttpStatus.BAD_REQUEST, "파일 삭제 시 오류가 발생했습니다."),

  DIRECT_FILE_UPLOAD_ERROR(HttpStatus.BAD_REQUEST, "파일 업로드 시 오류가 발생했습니다."),

  DIRECT_FILE_DELETE_ERROR(HttpStatus.BAD_REQUEST, "파일 삭제 시 오류가 발생했습니다."),

  INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다."),

  DUPLICATE_COURSE_UPLOAD(HttpStatus.BAD_REQUEST, "중복된 교과목명 파일입니다."),

  WRONG_COURSE_FILE_FORMAT(HttpStatus.BAD_REQUEST, "교과목명 업로드 파일 포맷 오류"),

  THUMBNAIL_CREATION_ERROR(HttpStatus.BAD_REQUEST, "썸네일 생성 중 오류 발생"),

  FILE_EMPTY(HttpStatus.BAD_REQUEST, "파일이 존재하지 않습니다."),

  FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."),

  FTP_DOWNLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "FTP 다운로드 중 오류가 발생했습니다."),

  EMPTY_OR_SINGLE_FILE_FOR_ZIP(HttpStatus.BAD_REQUEST, "압축할 파일 목록이 비어있거다 하나입니다"),

  FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 크기가 너무 큽니다."),

  INVALID_UPLOAD_TYPE(HttpStatus.BAD_REQUEST, "지원되지 않는 업로드 타입"),

  IMAGE_ZIP_UPLOAD_ERROR(HttpStatus.BAD_REQUEST, "이미지 ZIP 파일 생성 또는 업로드 중 오류"),

  DOCUMENT_FILES_EMPTY(HttpStatus.BAD_REQUEST, "DOCUMENT 업로드 타입인데 문서 파일이 없음"),

  IMAGE_FILES_EMPTY(HttpStatus.BAD_REQUEST, "IMAGE 업로드 타입인데 이미지 파일이 없음"),

  MEDIA_FILES_EMPTY(HttpStatus.BAD_REQUEST, "MEDIA 업로드 타입인데 미디어 파일이 없음"),

  UPLOAD_TYPE_NOT_SET(HttpStatus.BAD_REQUEST, "업로드 타입이 설정되지 않음"),

  FILE_LIST_EMPTY(HttpStatus.BAD_REQUEST, "업로드된 파일 목록이 비어있음"),

  VIDEO_FRAME_EXTRACTION_ERROR(HttpStatus.BAD_REQUEST, "비디오 프레임 추출 중 오류 발생"),

  FILE_PATH_EMPTY(HttpStatus.BAD_REQUEST, "파일 경로가 비어 있거나 null입니다."),

  PERCENTILE_CALCULATION_ERROR(HttpStatus.BAD_REQUEST, "백분위 계산 오류: 전체 기준이 0 일 수 없습니다."),

  INVALID_JSON_STRUCTURE(HttpStatus.BAD_REQUEST, "JSON 구조에 문제가 있습니다."),

  // FTP

  FTP_FILE_UPLOAD_ERROR(HttpStatus.BAD_REQUEST, "FTP 파일 업로드에 실패했습니다."),
  
  FTP_FILE_DELETE_ERROR(HttpStatus.BAD_REQUEST, "FTP 파일 삭제에 실패했습니다."),
  
  FTP_CONNECTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "FTP 서버 연결에 실패했습니다."),
  
  FTP_LOGIN_ERROR(HttpStatus.UNAUTHORIZED, "FTP 로그인에 실패했습니다."),

  // Department

  DEPARTMENT_IO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "학과 데이터를 로드하는 동안 IO 오류가 발생했습니다."),

  // Course

  FACULTY_NOT_FOUND(HttpStatus.NOT_FOUND, "교과목명에 해당하는 단과대를 찾을 수 없습니다."),

  COURSE_SAVE_ERROR(HttpStatus.BAD_REQUEST, "교과목명 파일 처리 중 오류가 발생했습니다"),

  WRONG_FACULTY_NAME(HttpStatus.BAD_REQUEST, "올바르지 않은 단과 대학입니다"),

  // Member

  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "회원이 존재하지 않습니다."),

  YEOPJEON_NOT_FOUND(HttpStatus.NOT_FOUND, "유저의 엽전 테이블이 존재하지 않습니다."),

  // QuestionPost

  QUESTION_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "질문 글을 찾을 수 없습니다."),

  QUESTION_TITLE_NULL(HttpStatus.BAD_REQUEST, "질문 게시글의 제목이 비어 있습니다."),

  QUESTION_CONTENT_NULL(HttpStatus.BAD_REQUEST, "질문 게시글의 본문이 비어 있습니다."),

  QUESTION_SUBJECT_NULL(HttpStatus.BAD_REQUEST, "과목이 설정되지 않았습니다."),

  QUESTION_PRESET_TAG_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "질문게시글 정적태그는 최대 2개까지 선택가능합니다."),

  QUESTION_INVALID_REWARD_YEOPJEON(HttpStatus.BAD_REQUEST, "질문게시글 엽전 현상금에 잘못된 값이 할당되었습니다."),

  // AnswerPost

  ANSWER_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "답변 글을 찾을 수 없습니다."),

  SELF_ANSWER_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "본인의 질문글에는 답변을 작성할 수 없습니다."),

  SELF_CHAETAEK_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "본인이 작성한 답변을 채택할 수 없습니다."),

  ONLY_AUTHOR_CAN_CHAETAEK(HttpStatus.BAD_REQUEST, "오직 질문 작성자만 답변을 채택할 수 있습니다."),

  CHAETAEK_ANSWER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "해당 질문글에 이미 채택된 답변이 존재합니다."),

  // Comment

  COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),

  COMMENT_LIKE_HISTORY_SAVE_ERROR(HttpStatus.BAD_REQUEST, "댓글 내역 저장 중 오류가 발생했습니다."),

  // DocumentPost

  DOCUMENT_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "자료 글을 찾을 수 없습니다."),

  DOCUMENT_TYPE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "자료게시글 자료 종류는 최대 2개까지 선택가능합니다."),

  INVALID_ATTENDED_YEAR(HttpStatus.BAD_REQUEST, "적절하지않은 수강년도입니다"),

  // DocumentFile

  DOCUMENT_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "자료파일을 찾을 수 없습니다."),

  // DocumentRequestPost

  DOCUMENT_REQUEST_POST_NOT_FOUND(HttpStatus.BAD_REQUEST, "자료 요청 글을 찾을 수 없습니다."),

  // NoticePost

  NOTICE_TITLE_NULL(HttpStatus.BAD_REQUEST, "공지사항 글의 제목이 비어있습니다."),

  NOTICE_CONTENT_NULL(HttpStatus.BAD_REQUEST, "공지사항 글의 본문이 비어있습니다."),

  NOTICE_POST_NOT_FOUND(HttpStatus.NOT_FOUND, "공지사항 글을 찾을 수 없습니다."),

  // PostTier

  INVALID_POST_TIER(HttpStatus.BAD_REQUEST, "잘못된 자료 등급 입니다."),

  // ContentType

  INVALID_CONTENT_TYPE(HttpStatus.BAD_REQUEST, "잘못된 ContentType 입니다."),

  // ReactionType

  INVALID_REACTION_TYPE(HttpStatus.BAD_REQUEST, "잘못된 ReactionType 입니다."),

  // SortType
  INVALID_SORT_TYPE(HttpStatus.BAD_REQUEST, "잘못된 정렬타입 입니다."),

  // MediaFile

  MEDIA_FILE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "첨부할 수 있는 미디어 파일 개수를 초과했습니다."),

  // CustomTag

  CUSTOM_TAG_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "커스텀태그는 최대 4개까지 추가할 수 있습니다."),

  CUSTOM_TAG_LENGTH_EXCEEDED(HttpStatus.BAD_REQUEST, "커스텀태그 길이는 최대 10자 입니다."),

  // Yeopjeon

  INSUFFICIENT_YEOPJEON(HttpStatus.BAD_REQUEST, "사용자의 엽전이 부족합니다."),

  YEOPJEON_ROLLBACK_ERROR(HttpStatus.BAD_REQUEST, "엽전 롤백 중 오류가 발생했습니다."),

  YEOPJEON_SAVE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "엽전 저장 시 오류가 발생했습니다."),

  YEOPJEON_HISTORY_SAVE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "엽전 히스토리 저장 중 오류가 발생했습니다."),

  YEOPJEON_HISTORY_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "엽전 히스토리 삭제 중 오류가 발생했습니다."),

  YEOPJEON_ACTION_NOT_FOUND(HttpStatus.NOT_FOUND, "올바르지않은 엽전 변동 사유입니다."),

  // Exp

  INSUFFICIENT_EXP(HttpStatus.BAD_REQUEST, "사용자의 경험치가 0 미만입니다."),

  EXP_SAVE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "경험치 저장 시 올류가 발생했습니다."),

  EXP_ROLLBACK_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "경험치 롤백 중 오류가 발생했습니다."),

  EXP_HISTORY_SAVE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "경험치 히스토리 저장 중 오류가 발생했습니다."),

  EXP_HISTORY_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "경험치 히스토리 삭제 중 오류가 발생했습니다."),

  EXP_NOT_FOUND(HttpStatus.NOT_FOUND, "경험치 객체를 찾을 수 없습니다."),

  INVALID_EXP_ACTION(HttpStatus.BAD_REQUEST, "잘못된 ExpAction에 해당됩니다."),

  // QuestionBoardLike & DocumentBoardLike

  QUESTION_BOARD_LIKE_SAVE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "질문게시판 좋아요 내역 저장 중 오류가 발생했습니다."),

  DOCUMENT_BOARD_LIKE_SAVE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "자료게시판 좋아요/싫어요 내역 저장 중 오류가 발생했습니다."),

  SELF_ACTION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "본인에게 좋아요 또는 싫어요를 누를 수 없습니다."),

  ALREADY_ACTION(HttpStatus.BAD_REQUEST, "이미 좋아요 또는 싫어요를 누른 글입니다."),

  LIKE_COUNT_CANNOT_BE_NEGATIVE(HttpStatus.BAD_REQUEST, "좋아요 개수의 최소값은 0입니다."),

  // Report

  ALREADY_REPORTED(HttpStatus.CONFLICT , "이미 해당 게시글을 신고하셨습니다."),

  // ErrorCode

  SERVER_ERROR_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "서버 에러코드를 찾을 수 없습니다.");

  private final HttpStatus status;
  private final String message;
}
