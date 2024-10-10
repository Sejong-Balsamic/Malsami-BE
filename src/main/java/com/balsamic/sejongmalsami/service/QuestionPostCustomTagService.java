package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.QuestionPostCustomTag;
import com.balsamic.sejongmalsami.repository.mongo.QuestionPostCustomTagRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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
  public Set<String> saveCustomTags(Set<String> customTags, UUID postId) {

    // 커스텀 태그는 4개까지만 추가가능
    if (customTags.size() > 4) {
      throw new CustomException(ErrorCode.CUSTOM_TAG_LIMIT_EXCEEDED);
    }

    // 커스텀 태그 10자 제한
    for (String tag : customTags) {
      if (tag.length() > 10) {
        throw new CustomException(ErrorCode.CUSTOM_TAG_LENGTH_EXCEEDED);
      }
    }

    return customTags.stream().map(tag -> {
      QuestionPostCustomTag questionPostCustomTag = QuestionPostCustomTag.builder()
          .questionPostId(postId)
          .customTag(tag)
          .build();
      questionPostCustomTagRepository.save(questionPostCustomTag);
      return tag; // 저장된 태그 반환
    }).collect(Collectors.toSet());
  }
}
