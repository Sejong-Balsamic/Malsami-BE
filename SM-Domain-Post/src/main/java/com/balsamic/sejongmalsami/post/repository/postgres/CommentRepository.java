package com.balsamic.sejongmalsami.post.repository.postgres;

import com.balsamic.sejongmalsami.constants.ContentType;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.post.object.postgres.Comment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

  Page<Comment> findByPostIdAndContentType(UUID postId, ContentType contentType, Pageable pageable);

  Long countByMember(Member member);

  List<Comment> findByMember(
      Member member);
}


