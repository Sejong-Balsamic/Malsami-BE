package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.postgres.DocumentFile;
import com.balsamic.sejongmalsami.postgres.Member;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentFileRepository extends JpaRepository<DocumentFile, UUID> {

  List<DocumentFile> findByDocumentPost_DocumentPostId(UUID documentPostId);

  List<DocumentFile> findByUploader(Member uploader);

  boolean existsByUploader(Member member);
}