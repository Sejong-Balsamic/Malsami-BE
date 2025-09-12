package com.balsamic.sejongmalsami.util;

import com.balsamic.sejongmalsami.constants.YeopjeonAction;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import com.balsamic.sejongmalsami.util.properties.YeopjeonProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class YeopjeonCalculator {

  private final YeopjeonProperties yeopjeonProperties;

  // YeopjeonAction 따른 엽전 변동량 계산
  public int calculateYeopjeon(YeopjeonAction action) {
    return switch (action) {
      case VIEW_DOCUMENT_CHEONMIN_POST -> yeopjeonProperties.getViewCheonminCost();
      case VIEW_DOCUMENT_JUNGIN_POST -> yeopjeonProperties.getViewJunginCost();
      case VIEW_DOCUMENT_YANGBAN_POST -> yeopjeonProperties.getViewYangbanCost();
      case VIEW_DOCUMENT_KING_POST -> yeopjeonProperties.getViewKingCost();
      case PURCHASE_DOCUMENT -> yeopjeonProperties.getPurchaseDocument();
      case DOCUMENT_UPLOADER_REWARD -> yeopjeonProperties.getDocumentFileUploaderReward();
      case RECEIVE_LIKE -> yeopjeonProperties.getLikeReward();
      case CANCEL_LIKE -> yeopjeonProperties.getCancelLike();
      case RECEIVE_DISLIKE -> yeopjeonProperties.getDislikePenalty();
      case CANCEL_DISLIKE -> yeopjeonProperties.getCancelDislike();
      case ATTENDANCE_BONUS -> yeopjeonProperties.getAttendanceBonus();
      case RECEIVE_REPORT_PENALTY -> yeopjeonProperties.getReportPenalty();
      case CREATE_QUESTION_POST -> yeopjeonProperties.getCreateQuestionPost();
      case DELETE_POST -> 0;
      case CHAETAEK_CHOSEN -> yeopjeonProperties.getChaetaekChosen();
      case CHAETAEK_ACCEPT -> yeopjeonProperties.getChaetaekAccept();
      case COPYRIGHT_VIOLATION -> yeopjeonProperties.getCopyrightViolation();
      case REPORT_REWARD -> yeopjeonProperties.getReportReward();
      case CREATE_ACCOUNT -> yeopjeonProperties.getCreateAccount();
      case REWARD_YEOPJEON -> 0;
      default -> throw new CustomException(ErrorCode.YEOPJEON_ACTION_NOT_FOUND);
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
