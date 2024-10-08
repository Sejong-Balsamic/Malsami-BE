package com.balsamic.sejongmalsami.object.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum YeopjeonAction {
  // TODO: 엽전 변동 사유 정하기

  VIEW_POST("게시글 열람"), // 게시글 조회 시 엽전 소모
  PURCHASE_DOCUMENT("자료 다운로드"), // 자료 구매 시 엽전 소모
  RECEIVE_LIKE("좋아요 받음"), // 좋아요 수신 시 엽전 획득
  ATTENDANCE_BONUS("출석 보너스"), // 출석 보너스 시 엽전 획득
  RECEIVE_DISLIKE("싫어요 받음"), // 싫어요 수신 시 엽전 소모
  RECEIVE_REPORT_PENALTY("신고 누적"), // 게시글 신고 누적으로 인한 엽전 깎임
  CREATE_QUESTION("질문게시글 작성"), // 게시글 작성 시 엽전 소모
  DELETE_POST("게시글 삭제"), // 게시글 삭제 시 엽전 환급
  ANSWER_CHAETAEK("답변 채택됨"), // 답변 채택 시 엽전 획득
  CLICK_CHAETAEK("답변 채택함"), // 답변 채택 버튼 클릭 시 엽전 획득
  COPYRIGHT_VIOLATION("저작권 위반"), // 저작권 위반 시 엽전 패널티
  FIRST_ATTENDANCE("신규 회원"); // 신규회원 엽전 지급

  private final String description;
}