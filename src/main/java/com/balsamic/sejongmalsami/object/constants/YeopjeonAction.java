package com.balsamic.sejongmalsami.object.constants;

public enum YeopjeonAction {
  VIEW_POST,                // 게시글 조회 시 엽전 소모
  CHANGE_POST_LEVEL,        // 게시글 등급 변경 시 엽전 소모
  PURCHASE_DOCUMENT,        // 자료 구매 시 엽전 소모
  RECEIVE_LIKE,             // 좋아요 수신 시 엽전 획득
  ATTENDANCE_BONUS,         // 출석 보너스 시 엽전 획득
  RECEIVE_DISLIKE,          // 싫어요 수신 시 엽전 소모
  RECEIVE_REPORT_PENALTY,   // 게시글 신고 누적으로 인한 엽전 깎임
  CREATE_POST,              // 게시글 작성 시 엽전 소모
  DELETE_POST,              // 게시글 삭제 시 엽전 환급
  ACCEPT_ANSWER             // 답변 채택 시 엽전 획득
}