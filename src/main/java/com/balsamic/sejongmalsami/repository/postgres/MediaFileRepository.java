package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.MediaFile;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaFileRepository extends JpaRepository<MediaFile, UUID> {
  
}
