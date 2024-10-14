package com.balsamic.sejongmalsami.repository.mongo;

import com.balsamic.sejongmalsami.object.DocumentFile;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentFileRepository extends JpaRepository<DocumentFile, UUID> {

  List<DocumentFile> findByPostId(UUID postId);

  List<DocumentFile> findByMemberId(UUID memberId);

}