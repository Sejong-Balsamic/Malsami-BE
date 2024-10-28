package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.postgres.DocumentFile;
import com.balsamic.sejongmalsami.object.postgres.Member;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentFileRepository extends JpaRepository<DocumentFile, UUID> {

  List<DocumentFile> findByPostId(UUID postId);

  List<DocumentFile> findByUploader(Member uploader);
}