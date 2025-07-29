package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.mongo.DocumentPostCustomTag;
import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.repository.mongo.DocumentPostCustomTagRepository;
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
public class DocumentPostCustomTagService {
  private static final Integer CUSTOM_TAG_LIMIT = 4;
  private static final Integer CUSTOM_TAG_LENGTH_LIMIT = 10;

  private final DocumentPostCustomTagRepository documentPostCustomTagRepository;

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

    for (String tag : customTags) {
      documentPostCustomTagRepository.save(
          DocumentPostCustomTag.builder()
          .documentPostId(postId)
          .customTag(tag)
          .build());
    }

    return customTags;
  }

  // 자료 엔티티 내부 customTags 필드 적용
  @Transactional
  public void findDocumentPostCustomTags(DocumentPost documentPost) {
    List<DocumentPostCustomTag> documentPostCustomTags =
        documentPostCustomTagRepository.findAllByDocumentPostId(documentPost.getDocumentPostId());
    List<String> customTags = documentPostCustomTags.stream()
        .map(DocumentPostCustomTag::getCustomTag)
        .collect(Collectors.toList());
    documentPost.setCustomTags(customTags);
  }
}
