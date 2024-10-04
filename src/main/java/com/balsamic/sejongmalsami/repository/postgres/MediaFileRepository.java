package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.AnswerPost;
import com.balsamic.sejongmalsami.object.MediaFile;
import com.balsamic.sejongmalsami.object.QuestionPost;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaFileRepository extends JpaRepository<MediaFile, UUID> {

  Integer countByQuestionPost(QuestionPost questionPost);

  Integer countByAnswerPost(AnswerPost answerPost);
}
