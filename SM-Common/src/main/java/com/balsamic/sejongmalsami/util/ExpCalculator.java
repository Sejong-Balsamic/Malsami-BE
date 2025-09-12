package com.balsamic.sejongmalsami.util;

import com.balsamic.sejongmalsami.constants.ExpAction;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import com.balsamic.sejongmalsami.util.properties.ExpProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExpCalculator {

  private final ExpProperties expProperties;

  // ExpAction에 따른 경험치 변동량 계산
  public int calculateExp(ExpAction expAction) {
    if (expAction.equals(ExpAction.CREATE_QUESTION_POST)) {
      return expProperties.getCreateQuestionPost();
    } else if (expAction.equals(ExpAction.CREATE_DOCUMENT_POST)) {
      return expProperties.getCreateDocumentPost();
    } else if (expAction.equals(ExpAction.CREATE_COMMENT)) {
      return expProperties.getCreateComment();
    } else if (expAction.equals(ExpAction.CREATE_ANSWER_POST)) {
      return expProperties.getCreateAnswerPost();
    } else if (expAction.equals(ExpAction.CHAETAEK_CHOSEN)) {
      return expProperties.getChaetaekChosen();
    } else if (expAction.equals(ExpAction.CHAETAEK_ACCEPT)) {
      return expProperties.getChaetaekAccept();
    } else if (expAction.equals(ExpAction.PURCHASE_DOCUMENT)) {
      return expProperties.getPurchaseDocument();
    } else if (expAction.equals(ExpAction.RECEIVE_LIKE)) {
      return expProperties.getReceiveLike();
    } else if (expAction.equals(ExpAction.CANCEL_LIKE)) {
      return expProperties.getCancelLike();
    } else {
      log.error("잘못된 ExpAction 입니다. 요청한 action: {}", expAction);
      throw new CustomException(ErrorCode.INVALID_EXP_ACTION);
    }

  }

}
