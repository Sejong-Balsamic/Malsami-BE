package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.postgres.DocumentFile;
import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.object.postgres.DocumentRequestPost;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Page;

@Builder
@Getter
@ToString
public class DocumentDto {

  private DocumentPost documentPost; // 자료
  private List<DocumentPost> documentPosts; // 자료
  private DocumentRequestPost documentRequestPost; // 자료요청
  private List<DocumentRequestPost> documentRequestPosts; // 자료요청

  private Page<DocumentPost> documentPostsPage;

  private List<DocumentFile> documentFiles;
}
