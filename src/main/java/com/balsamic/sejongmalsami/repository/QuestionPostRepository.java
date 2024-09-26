package com.balsamic.sejongmalsami.repository;

import com.balsamic.sejongmalsami.object.QuestionPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuestionPostRepository extends JpaRepository<QuestionPost, UUID> {

}