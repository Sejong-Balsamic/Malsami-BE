package com.balsamic.sejongmalsami.object.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum YeopjeonAction {

  VIEW_DOCUMENT_JUNGIN_POST("중인 자료 게시글 열람"), // 중인 게시글 조회 시 엽전 소모
  VIEW_DOCUMENT_YANGBAN_POST("양반 자료 게시글 열람"), // 양반 게시글 조회 시 엽전 소모
  VIEW_DOCUMENT_KING_POST("왕 자료 게시글 열람"), // 왕 게시글 조회 시 엽전 소모
  PURCHASE_DOCUMENT("자료 다운로드"), // 자료 구매 시 엽전 소모
  SEND_LIKE("좋아요 누름"), // 좋아요 누르면 엽전 획득
  RECEIVE_LIKE("좋아요 받음"), // 좋아요 수신 시 엽전 획득
  RECEIVE_DISLIKE("싫어요 받음"), // 싫어요 수신 시 엽전 회수
  ATTENDANCE_BONUS("출석 보너스"), // 출석 보너스 시 엽전 획득
  RECEIVE_REPORT_PENALTY("신고 누적"), // 게시글 신고 누적으로 인한 엽전 깎임
  CREATE_QUESTION_POST("질문 게시글 작성"), // 질문 게시글 작성 시 엽전 소모
  DELETE_POST("게시글 삭제"), // 게시글 삭제 시 엽전 환급
  CHAETAEK_CHOSEN("답변 채택됨"), // 답변 채택 시 엽전 획득
  CHAETAEK_ACCEPT("답변 채택함"), // 답변 채택 버튼 클릭 시 엽전 획득
  COPYRIGHT_VIOLATION("저작권 신고"), // 저작권 위반 시 엽전 회수
  REPORT_REWARD("신고 보상금"), // 신고 보상금
  CREATE_ACCOUNT("신규 회원"); // 신규회원 엽전 지급

  private final String description;
}