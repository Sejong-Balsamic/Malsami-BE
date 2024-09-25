package com.balsamic.sejongmalsami.repository;

import com.balsamic.sejongmalsami.object.AnswerPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AnswerPostRepository extends JpaRepository<AnswerPost, UUID> {
}
