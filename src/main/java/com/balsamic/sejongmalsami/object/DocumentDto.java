package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.mongo.DocumentBoardLike;
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
  private Page<DocumentPost> documentPostsPage;
  private DocumentRequestPost documentRequestPost; // 자료요청
  private Page<DocumentRequestPost> documentRequestPostsPage; // 자료요청
  private DocumentBoardLike documentBoardLike; // 자료 or 자료요청 좋아요 내역
  private List<String> customTags; // 커스텀 태그 리스트

  private List<DocumentFile> documentFiles;
  private byte[] fileBytes;
  private String fileName;
  private String mimeType;
}
