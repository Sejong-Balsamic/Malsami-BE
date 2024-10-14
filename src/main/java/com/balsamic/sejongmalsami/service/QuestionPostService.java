package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.MediaFile;
import com.balsamic.sejongmalsami.object.Member;
import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.object.QuestionPost;
import com.balsamic.sejongmalsami.object.constants.QuestionPresetTag;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionPostService {

  private final QuestionPostRepository questionPostRepository;
  private final MemberRepository memberRepository;
  private final QuestionPostCustomTagService questionPostCustomTagService;
  private final MediaFileService mediaFileService;

  /* 질문 게시글 등록 로직 */
  @Transactional
  public QuestionDto saveQuestionPost(QuestionCommand command) {

    Member member = memberRepository.findById(command.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    // 엽전 현상금 null인 경우 기본 0으로 설정
    if (command.getRewardYeopjeon() == null) {
      command.setRewardYeopjeon(0);
    } else if (command.getRewardYeopjeon() < 0) { // 음수 값으로 설정될 경우 오류
      throw new CustomException(ErrorCode.QUESTION_REWARD_INVALID);
    }

    QuestionPost questionPost = QuestionPost.builder()
        .member(member)
        .title(command.getTitle())
        .content(command.getContent())
        .subject(command.getSubject())
        .questionPresetTagSet(new HashSet<>())
        .viewCount(0)
        .likeCount(0)
        .answerCount(0)
        .commentCount(0)
        .rewardYeopjeon(command.getRewardYeopjeon())
        .dailyScore(0)
        .weeklyScore(0)
        .isPrivate(command.getIsPrivate() != null ? command.getIsPrivate() : false)
        .build();

    // 정적 태그 추가
    if (command.getQuestionPresetTagSet() != null) {
      for (QuestionPresetTag tag : command.getQuestionPresetTagSet()) {
        questionPost.addPresetTag(tag);
      }
    }

    QuestionPost savedPost = questionPostRepository.save(questionPost);

    // 커스텀 태그 추가
    Set<String> customTags = null;
    if (command.getCustomTagSet() != null) {
      customTags = questionPostCustomTagService
          .saveCustomTags(command.getCustomTagSet(), savedPost.getQuestionPostId());
    }

    List<MediaFile> mediaFiles = new ArrayList<>();
    // 첨부파일 추가
    if (command.getMediaFiles() != null && !command.getMediaFiles().isEmpty()) {
      command.getMediaFiles().forEach(file -> {
        try {
          MediaFile mediaFile = mediaFileService.uploadMediaFile(savedPost.getQuestionPostId(), file);
          mediaFiles.add(mediaFile);
        } catch (IOException e) {
          log.error("업로드 파일 리스트 = {}", mediaFiles);
          throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
        }
      });
    }

    return QuestionDto.builder()
        .questionPost(savedPost)
        .mediaFiles(mediaFiles)
        .customTags(customTags)
        .build();
  }
}
