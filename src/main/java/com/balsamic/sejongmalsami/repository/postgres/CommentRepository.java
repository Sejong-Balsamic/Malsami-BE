package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.Comment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

}
