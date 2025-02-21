package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.EmbeddingCommand;
import com.balsamic.sejongmalsami.object.EmbeddingDto;
import com.balsamic.sejongmalsami.object.GeneralPost;
import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.constants.FileStatus;
import com.balsamic.sejongmalsami.object.mongo.DocumentPostCustomTag;
import com.balsamic.sejongmalsami.object.mongo.QuestionPostCustomTag;
import com.balsamic.sejongmalsami.object.mongo.SearchHistory;
import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.object.postgres.PostEmbedding;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.repository.mongo.DocumentPostCustomTagRepository;
import com.balsamic.sejongmalsami.repository.mongo.QuestionPostCustomTagRepository;
import com.balsamic.sejongmalsami.repository.mongo.SearchHistoryRepository;
import com.balsamic.sejongmalsami.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.PostEmbeddingRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.util.CommonUtil;
import com.balsamic.sejongmalsami.util.RedisLockManager;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostEmbeddingService {

  private final OpenAIEmbeddingService embeddingService;
  private final SearchQueryCacheService searchQueryCacheService;
  private final PostEmbeddingRepository postEmbeddingRepository;
  private final QuestionPostRepository questionPostRepository;
  private final DocumentPostRepository documentPostRepository;
  private final QuestionPostCustomTagRepository questionPostCustomTagRepository;
  private final DocumentPostCustomTagRepository documentPostCustomTagRepository;
  private final SearchHistoryRepository searchHistoryRepository;
  private final RedisLockManager redisLockManager;

  private static final Long WAIT_TIME = 5L;
  private static final Long LEASE_TIME = 2L;

  /**
   * 새로운 질문/자료 글 작성 시 postEmbedding 저장
   */
  @Async
  @Transactional
  public void saveEmbedding(UUID postId, String text, ContentType contentType) {
    log.info("Embedding 저장 시작 - Post ID: {}, ContentType: {}", postId, contentType);

    PostEmbedding postEmbedding = PostEmbedding.builder()
        .postId(postId)
        .embedding(null)
        .contentType(contentType)
        .fileStatus(FileStatus.IN_PROGRESS)
        .message(null)
        .build();
    postEmbeddingRepository.save(postEmbedding);

    try {
      // Embedding 벡터 생성
      float[] embeddingArray = embeddingService.generateEmbedding(text);
      log.info("Embedding 생성 완료 - Post ID: {}", postId);

      // 정상 결과 업데이트
      postEmbedding.setEmbedding(embeddingArray);
      postEmbedding.setFileStatus(FileStatus.SUCCESS);
      postEmbedding.setMessage("Embedding 생성 성공");

      postEmbeddingRepository.save(postEmbedding);
      log.info("Embedding 저장 완료 - Post ID: {}", postId);

    } catch (Exception e) {
      // 오류 발생 시 상태 업데이트
      postEmbedding.setFileStatus(FileStatus.FAILURE);
      postEmbedding.setMessage(e.getMessage());
      postEmbeddingRepository.save(postEmbedding);
      log.error("Embedding 저장 중 오류 발생 - Post ID: {}, 오류: {}", postId, e.getMessage(), e);

      throw new CustomException(ErrorCode.EMBEDDING_GENERATION_FAILED);
    }
  }

  /**
   * 검색어 입력 시 검색어 Embedding 값과 유사한 글 반환
   */
  public EmbeddingDto searchSimilarEmbeddingsByText(EmbeddingCommand command) {
    try {
      // 캐시를 통해 embedding 확보
      float[] queryVectorArray = searchQueryCacheService.getOrCreateEmbedding(command.getText());

      // float[] -> "[...]" 문자열 변환
      String vectorString = CommonUtil.floatArrayToString(queryVectorArray);

      // ContentType 처리
      String contentType = command.getContentType() != null ? command.getContentType().name() : null;

      // 페이징 처리
      Pageable pageable = PageRequest.of(command.getPageNumber(), command.getPageSize());

      // 페이징 요청 및 반환
      Page<PostEmbedding> postEmbeddingsPage = postEmbeddingRepository.findSimilarEmbeddings(
          vectorString,
          command.getThreshold(),
          contentType,
          pageable
      );

      // Page<PostEmbedding> -> Page<GeneralPost> 변환
      Page<GeneralPost> generalPostsPage = postEmbeddingsPage.map(embedding -> {
        if (embedding.getContentType() == ContentType.QUESTION) {
          // QuestionPost 처리
          QuestionPost post = questionPostRepository.findById(embedding.getPostId())
              .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_POST_NOT_FOUND));

          List<String> customTags = questionPostCustomTagRepository
              .findAllByQuestionPostId(post.getQuestionPostId())
              .stream()
              .map(QuestionPostCustomTag::getCustomTag)
              .collect(Collectors.toList());

          return GeneralPost.fromQuestionPost(post, customTags);

        } else if (embedding.getContentType() == ContentType.DOCUMENT) {
          // DocumentPost 처리
          DocumentPost post = documentPostRepository.findById(embedding.getPostId())
              .orElseThrow(() -> new CustomException(ErrorCode.DOCUMENT_POST_NOT_FOUND));

          List<String> customTags = documentPostCustomTagRepository
              .findAllByDocumentPostId(post.getDocumentPostId())
              .stream()
              .map(DocumentPostCustomTag::getCustomTag)
              .collect(Collectors.toList());

          return GeneralPost.fromDocumentPost(post, customTags);
        }

        throw new CustomException(ErrorCode.INVALID_CONTENT_TYPE);
      });

      String lockKey = "lock:searchHistory" + command.getText();
      redisLockManager.executeLock(lockKey, WAIT_TIME, LEASE_TIME, () -> {
        // 검색어 히스토리 저장
        SearchHistory searchHistory = searchHistoryRepository
            .findByKeyword(command.getText()).orElseGet(() ->
                searchHistoryRepository.save(SearchHistory.builder()
                    .keyword(command.getText())
                    .searchCount(0L)
                    .lastRank(-1)
                    .currentRank(-1)
                    .rankChange(0)
                    .isNew(false)
                    .build())
            );
        searchHistory.increaseSearchCount(); // 검색횟수 1증가
        searchHistoryRepository.save(searchHistory);
        return true;
      });

      return EmbeddingDto.builder()
          .generalPostsPage(generalPostsPage)
          .build();

    } catch (Exception e) {
      log.error("Embedding 검색 중 오류 발생: {}", e.getMessage(), e);
      throw new CustomException(ErrorCode.EMBEDDING_SEARCH_FAILED);
    }
  }
}
