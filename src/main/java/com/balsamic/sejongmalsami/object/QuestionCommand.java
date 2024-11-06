package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.constants.Faculty;
import com.balsamic.sejongmalsami.object.constants.QuestionPresetTag;
import com.balsamic.sejongmalsami.object.constants.SortType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class QuestionCommand {

  private UUID postId; // 질문, 답변
  private UUID memberId; // 질문, 답변
  private String title; // 질문
  private UUID questionPostId; // 답변
  private String content; // 질문, 답변
  private String subject; // 질문
  private List<MultipartFile> mediaFiles; // 질문, 답변
  private List<QuestionPresetTag> questionPresetTags; // 질문
  private List<String> customTagSet; // 질문
  private Integer rewardYeopjeon; // 질문
  private ContentType contentType;
  private Boolean isPrivate; // 질문, 답변
  @Schema(defaultValue = "0")
  private Integer pageNumber = 0; // n번째 페이지 조회
  @Schema(defaultValue = "30")
  private Integer pageSize = 30; // n개의 데이터 조회

  // 핕터링 파라미터
  private Faculty faculty; // 단과대별 조회 (필터링)
  private Integer minYeopjeon; // 최소 엽전 (필터링)
  private Integer maxYeopjeon; // 최대 엽전 (필터링)
  private SortType sortType; // 정렬 타입 (최신순, 좋아요순, 엽전현상금순, 조회순)
  private Boolean viewNotChaetaek; // 채택 안된 글
}
