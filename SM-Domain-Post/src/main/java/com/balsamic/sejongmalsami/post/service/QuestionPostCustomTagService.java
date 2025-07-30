package com.balsamic.sejongmalsami.post.service;

import com.balsamic.sejongmalsami.post.object.mongo.QuestionPostCustomTag;
import com.balsamic.sejongmalsami.post.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.post.repository.mongo.QuestionPostCustomTagRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.List;
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

  private static final Integer CUSTOM_TAG_LIMIT = 4;
  private static final Integer CUSTOM_TAG_LENGTH_LIMIT = 10;

  private final QuestionPostCustomTagRepository questionPostCustomTagRepository;

  // 커스텀 태그 저장 로직
  @Transactional
  public List<String> saveCustomTags(List<String> customTags, UUID postId) {

    // 커스텀 태그는 4개까지만 추가가능
    if (customTags.size() > CUSTOM_TAG_LIMIT) {
      throw new CustomException(ErrorCode.CUSTOM_TAG_LIMIT_EXCEEDED);
    }

    // 커스텀 태그 10자 제한
    for (String tag : customTags) {
      if (tag.length() > CUSTOM_TAG_LENGTH_LIMIT) {
        throw new CustomException(ErrorCode.CUSTOM_TAG_LENGTH_EXCEEDED);
      }
    }

    return customTags.stream()
        .map(tag -> questionPostCustomTagRepository.save(
            QuestionPostCustomTag.builder()
                .questionPostId(postId)
                .customTag(tag)
                .build()
        ).getCustomTag()) // 저장 후 태그 반환
        .collect(Collectors.toList());
  }

  /**
   * 질문글에 등록된 커스텀태그를 조회 후 내부 필드에 등록
   */
  @Transactional(readOnly = true)
  public void findQuestionPostCustomTags(QuestionPost questionPost) {
    List<QuestionPostCustomTag> questionPostCustomTags =
        questionPostCustomTagRepository.findAllByQuestionPostId(questionPost.getQuestionPostId());
    List<String> customTags = questionPostCustomTags.stream()
        .map(QuestionPostCustomTag::getCustomTag)
        .collect(Collectors.toList());
    questionPost.setCustomTags(customTags);
  }
}
