package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.postgres.DocumentRequestPost;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRequestPostRepository extends JpaRepository<DocumentRequestPost, UUID> {

}
