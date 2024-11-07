package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.constants.ExpAction;
import com.balsamic.sejongmalsami.object.postgres.Comment;
import com.balsamic.sejongmalsami.object.CommentCommand;
import com.balsamic.sejongmalsami.object.CommentDto;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.repository.postgres.CommentRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final MemberRepository memberRepository;
  private final ExpService expService;

  /**
   * <h3>댓글 작성 로직
   * <p>작성자 경험치 증가 및 경험치 내역 저장
   * @param command
   * @return
   */
  @Transactional
  public CommentDto addComment(CommentCommand command) {

    Member member = memberRepository.findById(command.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    // 댓글 작성자 경험치 증가 및 경험치 히스토리 저장
    expService.updateExpAndSaveExpHistory(member, ExpAction.CREATE_COMMENT);

    return CommentDto.builder()
        .comment(commentRepository.save(Comment.builder()
            .member(member)
            .content(command.getContent())
            .postId(command.getPostId())
            .contentType(command.getContentType())
            .isPrivate(command.getIsPrivate() != null ? command.getIsPrivate() : false)
            .build()))
        .build();
  }

}
