package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.constants.PopularType;
import com.balsamic.sejongmalsami.object.postgres.PopularPost;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopularPostRepository extends JpaRepository<PopularPost, UUID> {

  // (질문 or 자료) 글 (일간 or 주간) 인기글 삭제
  void deleteAllByContentTypeAndPopularType(ContentType contentType, PopularType popularType);

  // (질문 or 자료) 글 (일간 or 주간) 인기글 조회
  Page<PopularPost> findAllByContentTypeAndPopularType(ContentType contentType, PopularType popularType, Pageable pageable);
}
