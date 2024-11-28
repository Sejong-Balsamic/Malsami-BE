package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.postgres.NoticePost;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticePostRepository extends JpaRepository<NoticePost, UUID> {
}
