package com.balsamic.sejongmalsami.repository;

import com.balsamic.sejongmalsami.object.QuestionPost;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionPostRepository extends JpaRepository<QuestionPost, UUID> {

}