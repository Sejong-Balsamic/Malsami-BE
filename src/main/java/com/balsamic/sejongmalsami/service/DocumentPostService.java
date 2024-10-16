package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.object.DocumentPostCommand;
import com.balsamic.sejongmalsami.object.DocumentPostDto;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.constants.DocumentType;
import com.balsamic.sejongmalsami.object.constants.PostTier;
import com.balsamic.sejongmalsami.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.HashSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentPostService {

  private final DocumentPostRepository documentPostRepository;
  private final MemberRepository memberRepository;

  // 질문 게시글 등록
  @Transactional
  public DocumentPostDto saveDocumentPost(DocumentPostCommand command) {

    Member member = memberRepository.findById(command.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    DocumentPost documentPost = DocumentPost.builder()
        .member(member)
        .title(command.getTitle())
        .content(command.getContent())
        .subject(command.getSubject())
        .documentTypeSet(new HashSet<>())
        .postTier(PostTier.CHEONMIN)
        .likeCount(0)
        .commentCount(0)
        .viewCount(0)
        .isDepartmentPrivate(command.getIsDepartmentPrivate() != null ? command.getIsDepartmentPrivate() : false)
        .dailyScore(0)
        .weeklyScore(0)
        .build();

    // 자료 카테고리 추가
    if (command.getDocumentTypeSet() != null) {
      for (DocumentType documentType : command.getDocumentTypeSet()) {
        documentPost.addDocumentType(documentType);
      }
    }

    return DocumentPostDto.builder()
        .documentPost(documentPostRepository.save(documentPost))
        .build();
  }
}
