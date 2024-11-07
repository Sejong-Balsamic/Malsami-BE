package com.balsamic.sejongmalsami.util;

import com.balsamic.sejongmalsami.object.constants.ExpAction;
import com.balsamic.sejongmalsami.util.config.ExpConfig;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExpCalculator {

  private final ExpConfig expConfig;

  // ExpAction에 따른 경험치 변동량 계산
  public int calculateExp(ExpAction expAction) {
    if (expAction.equals(ExpAction.CREATE_QUESTION_POST)) {
      return expConfig.getCreateQuestionPost();
    } else if (expAction.equals(ExpAction.CREATE_DOCUMENT_POST)) {
      return expConfig.getCreateDocumentPost();
    } else if (expAction.equals(ExpAction.CREATE_COMMENT)) {
      return expConfig.getCreateComment();
    } else if (expAction.equals(ExpAction.CREATE_ANSWER_POST)) {
      return expConfig.getCreateAnswerPost();
    } else if (expAction.equals(ExpAction.CHAETAEK_CHOSEN)) {
      return expConfig.getChaetaekChosen();
    } else if (expAction.equals(ExpAction.CHAETAEK_ACCEPT)) {
      return expConfig.getChaetaekAccept();
    } else if (expAction.equals(ExpAction.PURCHASE_DOCUMENT)) {
      return expConfig.getPurchaseDocument();
    } else if (expAction.equals(ExpAction.RECEIVE_LIKE)) {
      return expConfig.getReceiveLike();
    } else {
      throw new CustomException(ErrorCode.INVALID_EXP_ACTION);
    }

  }

}
