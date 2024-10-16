package com.balsamic.sejongmalsami.service;

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

  // 댓글 추가
  @Transactional
  public CommentDto addComment(CommentCommand command) {

    Member member = memberRepository.findById(command.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    Comment comment = commentRepository.save(Comment.builder()
        .member(member)
        .content(command.getContent())
        .postId(command.getPostId())
        .contentType(command.getContentType())
        .isPrivate(command.getIsPrivate() != null ? command.getIsPrivate() : false)
        .build());

    return CommentDto.builder()
        .comment(comment)
        .build();
  }

}
