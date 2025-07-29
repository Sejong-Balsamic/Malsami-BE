package com.balsamic.sejongmalsami.post.service;

import static com.balsamic.sejongmalsami.object.constants.SortType.COMMENT_COUNT;
import static com.balsamic.sejongmalsami.object.constants.SortType.LATEST;
import static com.balsamic.sejongmalsami.object.constants.SortType.MOST_LIKED;
import static com.balsamic.sejongmalsami.object.constants.SortType.OLDEST;
import static com.balsamic.sejongmalsami.object.constants.SortType.VIEW_COUNT;
import static com.balsamic.sejongmalsami.object.constants.SortType.getNativeQuerySortOrder;

import com.balsamic.sejongmalsami.object.QueryCommand;
import com.balsamic.sejongmalsami.object.constants.SortType;
import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.object.postgres.DocumentRequestPost;
import com.balsamic.sejongmalsami.object.postgres.NoticePost;
import com.balsamic.sejongmalsami.post.dto.QueryDto;
import com.balsamic.sejongmalsami.postgres.QuestionPost;
import com.balsamic.sejongmalsami.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.DocumentRequestPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.NoticePostRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.util.RedisLockManager;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class QueryService {

  private final QuestionPostRepository questionPostRepository;
  private final DocumentPostRepository documentPostRepository;
  private final DocumentRequestPostRepository documentRequestPostRepository;
  private final NoticePostRepository noticePostRepository;
  private final SearchHistoryService searchHistoryService;
  private final RedisLockManager redisLockManager;

  private static final Long WAIT_TIME = 5L;
  private static final Long LEASE_TIME = 2L;

  /**
   * 검색 로직
   * 제목+본문에 검색어를 포함하는 글을 반환합니다.
   * 해당 과목명이 포함된 글을 반환합니다.
   * 최신순, 좋아요순, 조회순, 과거순 정렬이 가능합니다.
   *
   * @param command query, subject, sortType, pageNumber, pageSize
   * @return
   */
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

    // 정렬타입 기본값 설정 / 잘못된 경우
    SortType sortType = (command.getSortType() != null) ? command.getSortType() : LATEST;
    if (!sortType.equals(LATEST) &&
        !sortType.equals(MOST_LIKED) &&
        !sortType.equals(COMMENT_COUNT) &&
        !sortType.equals(VIEW_COUNT) &&
        !sortType.equals(OLDEST)) {
      throw new CustomException(ErrorCode.INVALID_SORT_TYPE);
    }

    // 정렬조건 (최신순, 좋아요순, 댓글순, 조회순, 과거순)
    Sort sort = getNativeQuerySortOrder(sortType);

    // 검색
    Pageable pageable = PageRequest.of(
        command.getPageNumber(),
        command.getPageSize(),
        sort
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
    Page<NoticePost> noticePostPage = noticePostRepository
        .findNoticePostsByQuery(
            command.getQuery(),
            pageable
        );

    String lockKey = "lock:searchHistory" + command.getQuery();
    redisLockManager.executeLock(lockKey, WAIT_TIME, LEASE_TIME, () -> {
      // 검색어 히스토리 저장
      searchHistoryService.increaseSearchCount(command.getQuery());
      return true;
    });

    return QueryDto.builder()
        .questionPostsPage(questionPostPage)
        .documentPostsPage(documentPostPage)
        .documentRequestPostsPage(documentRequestPostPage)
        .noticePostsPage(noticePostPage)
        .build();
  }
}
