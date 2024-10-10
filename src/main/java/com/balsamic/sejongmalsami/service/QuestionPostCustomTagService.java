package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.QuestionPostCustomTag;
import com.balsamic.sejongmalsami.repository.mongo.QuestionPostCustomTagRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionPostCustomTagService {

  private final QuestionPostCustomTagRepository questionPostCustomTagRepository;

  // 커스텀 태그 저장
  @Transactional
  public void saveCustomTags(Set<String> customTags, UUID postId) {

    // 커스텀 태그는 4개까지만 추가가능
    if (customTags.size() > 4) {
      throw new CustomException(ErrorCode.QUESTION_CUSTOM_TAG_LIMIT_EXCEEDED);
    }

    for (String tag : customTags) {
      QuestionPostCustomTag questionPostCustomTag = QuestionPostCustomTag.builder()
          .questionPostId(postId)
          .customTag(tag)
          .build();
      questionPostCustomTagRepository.save(questionPostCustomTag);
    }
  }
}
