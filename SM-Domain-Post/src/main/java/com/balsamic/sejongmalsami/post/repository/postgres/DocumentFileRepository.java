package com.balsamic.sejongmalsami.post.repository.postgres;

import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.post.object.postgres.DocumentFile;
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