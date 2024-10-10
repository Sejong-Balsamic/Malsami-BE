package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.DocumentPost;
import com.balsamic.sejongmalsami.object.DocumentPostCommand;
import com.balsamic.sejongmalsami.object.DocumentPostDto;
import com.balsamic.sejongmalsami.object.Member;
import com.balsamic.sejongmalsami.object.constants.PostTier;
import com.balsamic.sejongmalsami.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentPostService {

  private final DocumentPostRepository documentPostRepository;
  private final MemberRepository memberRepository;

  // 질문 게시글 등록
  public DocumentPostDto saveDocumentPost(DocumentPostCommand command) {

    Member member = memberRepository.findById(command.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    DocumentPost documentPost = DocumentPost.builder()
        .member(member)
        .title(command.getTitle())
        .content(command.getContent())
        .subject(command.getSubject())
        .postTier(PostTier.CHEONMIN)
        .likeCount(0)
        .downloadCount(0)
        .commentCount(0)
        .viewCount(0)
        .isDepartmentPrivate(false)
        .dailyScore(0)
        .weeklyScore(0)
        .build();

    return DocumentPostDto.builder()
        .documentPost(documentPostRepository.save(documentPost))
        .build();
  }
}
