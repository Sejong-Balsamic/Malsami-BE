package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.QueryCommand;
import com.balsamic.sejongmalsami.object.QueryDto;
import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.object.postgres.DocumentRequestPost;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.DocumentRequestPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class QueryService {

  private final QuestionPostRepository questionPostRepository;
  private final DocumentPostRepository documentPostRepository;
  private final DocumentRequestPostRepository documentRequestPostRepository;

  /**
   * <h3>검색 로직
   * <p>제목+본문에 검색어를 포함하는 글을 반환합니다.</p>
   * <p>해당 과목명이 포함된 글을 반환합니다.</p>
   *
   * @param command query, subject, pageNumber, pageSize
   * @return
   */
  // TODO: 공지사항 글 추가
  @Transactional(readOnly = true)
  public QueryDto getPostsByQuery(QueryCommand command) {

    // query가 비어있는 경우 null 설정 (비어있는 경우 쿼리문에서 오류 발생)
    if (command.getQuery() != null && command.getQuery().isEmpty()) {
      command.setQuery(null);
    }

    // 과목명이 비어있는 경우 null 설정 (비어있는 경우 쿼리문에서 오류 발생)
    if (command.getSubject() != null && command.getSubject().isEmpty()) {
      command.setSubject(null);
    }

    // 검색 (최신순 정렬)
    Pageable pageable = PageRequest.of(
        command.getPageNumber(),
        command.getPageSize()
//        Sort.by("createdDate").descending()
    );

    Page<QuestionPost> questionPostPage = questionPostRepository
        .findQuestionPostsByQuery(
            command.getQuery(),
            command.getSubject(),
            pageable
        );
    Page<DocumentPost> documentPostPage = documentPostRepository
        .findDocumentPostsByQuery(
            command.getQuery(),
            command.getSubject(),
            pageable
        );
    Page<DocumentRequestPost> documentRequestPostPage = documentRequestPostRepository
        .findDocumentRequestPostsByQuery(
            command.getQuery(),
            command.getSubject(),
            pageable
        );

    return QueryDto.builder()
        .questionPostsPage(questionPostPage)
        .documentPostsPage(documentPostPage)
        .documentRequestPostsPage(documentRequestPostPage)
        .noticePostsPage(null)
        .build();
  }
}
