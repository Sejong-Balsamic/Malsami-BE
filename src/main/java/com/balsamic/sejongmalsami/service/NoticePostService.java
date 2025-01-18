package com.balsamic.sejongmalsami.service;

import static com.balsamic.sejongmalsami.object.constants.SortType.LATEST;
import static com.balsamic.sejongmalsami.object.constants.SortType.MOST_LIKED;
import static com.balsamic.sejongmalsami.object.constants.SortType.OLDEST;
import static com.balsamic.sejongmalsami.object.constants.SortType.VIEW_COUNT;
import static com.balsamic.sejongmalsami.object.constants.SortType.getJpqlSortOrder;

import com.balsamic.sejongmalsami.object.NoticePostCommand;
import com.balsamic.sejongmalsami.object.NoticePostDto;
import com.balsamic.sejongmalsami.object.postgres.NoticePost;
import com.balsamic.sejongmalsami.repository.postgres.NoticePostRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NoticePostService {

  private final NoticePostRepository noticePostRepository;

  /**
   * 공지사항 필터링 글 조회 필터링 조건에 맞는 전체 글을 반환합니다
   * <p>
   * [필터링] 1. 제목 필터링 (검색어 포함)
   * <p>
   * [정렬 조건] 1. 최신순 2. 과거순 3. 조회순 4. 추천순
   *
   * @param command query, sortType, pageNumber, pageSize
   * @return
   */
  public NoticePostDto getFilteredPost(NoticePostCommand command) {

    // query가 비어있는 경우 null 설정
    if (command.getQuery().isBlank()) {
      command.setQuery(null);
    }

    // 정렬 조건
    if (!command.getSortType().equals(LATEST) &&
        !command.getSortType().equals(OLDEST) &&
        !command.getSortType().equals(VIEW_COUNT) &&
        !command.getSortType().equals(MOST_LIKED)) {
      log.error("잘못된 정렬 조건입니다. 요청 SortType: {}", command.getSortType());
      throw new CustomException(ErrorCode.INVALID_SORT_TYPE);
    }
    Sort sort = getJpqlSortOrder(command.getSortType());

    // Pageable
    Pageable pageable = PageRequest.of(
        command.getPageNumber(),
        command.getPageSize(),
        sort
    );

    Page<NoticePost> noticePostPage = noticePostRepository
        .findNoticePostsByFilter(command.getQuery(), pageable);

    return NoticePostDto.builder()
        .noticePostsPage(noticePostPage)
        .build();
  }

}
