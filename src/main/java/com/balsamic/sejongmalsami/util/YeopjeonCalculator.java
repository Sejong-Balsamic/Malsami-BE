package com.balsamic.sejongmalsami.util;

import com.balsamic.sejongmalsami.object.constants.YeopjeonAction;
import com.balsamic.sejongmalsami.util.config.YeopjeonConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class YeopjeonCalculator {

  private final YeopjeonConfig yeopjeonConfig;

  // YeopjeonAction 따른 엽전 변동량 계산
  public int calculateYeopjeon(YeopjeonAction action) {
    return switch (action) {
      case VIEW_DOCUMENT_CHEONMIN_POST -> yeopjeonConfig.getViewCheonminCost();
      case VIEW_DOCUMENT_JUNGIN_POST -> yeopjeonConfig.getViewJunginCost();
      case VIEW_DOCUMENT_YANGBAN_POST -> yeopjeonConfig.getViewYangbanCost();
      case VIEW_DOCUMENT_KING_POST -> yeopjeonConfig.getViewKingCost();
      case PURCHASE_DOCUMENT -> yeopjeonConfig.getPurchaseDocument();
      case RECEIVE_LIKE -> yeopjeonConfig.getLikeReward();
      case RECEIVE_DISLIKE -> yeopjeonConfig.getDislikePenalty();
      case ATTENDANCE_BONUS -> yeopjeonConfig.getAttendanceBonus();
      case RECEIVE_REPORT_PENALTY -> yeopjeonConfig.getReportPenalty();
      case CREATE_QUESTION_POST -> yeopjeonConfig.getCreateQuestionPost();
      case DELETE_POST -> 0;
      case CHAETAEK_CHOSEN -> yeopjeonConfig.getChaetaekChosen();
      case CHAETAEK_ACCEPT -> yeopjeonConfig.getChaetaekAccept();
      case COPYRIGHT_VIOLATION -> yeopjeonConfig.getCopyrightViolation();
      case REPORT_REWARD -> yeopjeonConfig.getReportReward();
      case CREATE_ACCOUNT -> yeopjeonConfig.getCreateAccount();
      case REWARD_YEOPJEON -> 0;
    };
  }

  // YeopjeonAction과 커스텀 엽전 값에 따른 엽전 변동량 계산
  public int calculateYeopjeon(YeopjeonAction action, Integer customAmount) {
    if (action == YeopjeonAction.REWARD_YEOPJEON && customAmount != null) {
      return customAmount;
    }
    return calculateYeopjeon(action);
  }
}
