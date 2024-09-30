package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.Comment;
import com.balsamic.sejongmalsami.object.CommentCommand;
import com.balsamic.sejongmalsami.object.CommentDto;
import com.balsamic.sejongmalsami.object.Member;
import com.balsamic.sejongmalsami.repository.CommentRepository;
import com.balsamic.sejongmalsami.repository.MemberRepository;
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

    Comment comment = Comment.builder()
        .member(member)
        .content(command.getContent())
        .postId(command.getPostId())
        .postType(command.getPostType())
        .isPrivate(command.getIsPrivate())
        .build();

    commentRepository.save(comment);

    return CommentDto.builder()
        .comment(comment)
        .build();
  }

}
