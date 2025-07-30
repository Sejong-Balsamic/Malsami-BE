package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.postgres.PostEmbedding;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostEmbeddingRepository extends JpaRepository<PostEmbedding, UUID> {

  @Query(value = """
        SELECT *
        FROM post_embedding
        WHERE (content_type = :contentType OR :contentType IS NULL)
        AND CAST(embedding AS vector(1536)) <-> CAST(:queryVector AS vector(1536)) < :threshold
        ORDER BY CAST(embedding AS vector(1536)) <-> CAST(:queryVector AS vector(1536))
          """,
      countQuery = """
          SELECT COUNT(*)
          FROM post_embedding
          WHERE (content_type = :contentType OR :contentType IS NULL)
          AND CAST(embedding AS vector(1536)) <-> CAST(:queryVector AS vector(1536)) < :threshold
            """,
      nativeQuery = true)
  Page<PostEmbedding> findSimilarEmbeddings(
      @Param("queryVector") String queryVector,
      @Param("threshold") float threshold,
      @Param("contentType") String contentType,
      Pageable pageable);

  Optional<PostEmbedding> findByPostId(UUID postId);
}

