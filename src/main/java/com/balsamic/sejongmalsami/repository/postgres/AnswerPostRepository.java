package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.AnswerPost;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerPostRepository extends JpaRepository<AnswerPost, UUID> {

}
