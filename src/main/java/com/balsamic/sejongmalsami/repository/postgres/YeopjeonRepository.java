package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.Yeopjeon;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface YeopjeonRepository extends JpaRepository<Yeopjeon, UUID> {

}
