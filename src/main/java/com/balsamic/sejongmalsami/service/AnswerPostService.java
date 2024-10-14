package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.AnswerPost;
import com.balsamic.sejongmalsami.object.MediaFile;
import com.balsamic.sejongmalsami.object.Member;
import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.object.QuestionPost;
import com.balsamic.sejongmalsami.repository.postgres.AnswerPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AnswerPostService {

  private final AnswerPostRepository answerPostRepository;
  private final MemberRepository memberRepository;
  private final QuestionPostRepository questionPostRepository;
  private final MediaFileService mediaFileService;

  // 답변 작성 로직
  public QuestionDto saveAnswer(QuestionCommand command) {

    Member member = memberRepository.findById(command.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    QuestionPost questionPost = questionPostRepository.findById(command.getQuestionPostId())
        .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_POST_NOT_FOUND));

    AnswerPost answerPost = AnswerPost.builder()
        .member(member)
        .questionPost(questionPost)
        .content(command.getContent())
        .likeCount(0)
        .commentCount(0)
        .isPrivate(command.getIsPrivate() != null ? command.getIsPrivate() : false)
        .isChaetaek(false)
        .build();

    AnswerPost savedPost = answerPostRepository.save(answerPost);

    List<MediaFile> mediaFiles = new ArrayList<>();
    // 첨부파일 추가 로직
    if (command.getMediaFiles() != null && !command.getMediaFiles().isEmpty()) {
      command.getMediaFiles().forEach(file -> {
        try {
          MediaFile mediaFile = mediaFileService.uploadMediaFile(savedPost.getAnswerPostId(), file);
          mediaFiles.add(mediaFile);
        } catch (IOException e) {
          log.error("업로드 파일 리스트 = {}", mediaFiles);
          throw new CustomException(ErrorCode.FILE_UPLOAD_ERROR);
        }
      });
    }

    return QuestionDto.builder()
        .answerPost(savedPost)
        .mediaFiles(mediaFiles)
        .build();
  }
}
