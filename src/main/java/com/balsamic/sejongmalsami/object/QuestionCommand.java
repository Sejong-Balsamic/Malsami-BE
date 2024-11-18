package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.ChaetaekStatus;
import com.balsamic.sejongmalsami.object.constants.ContentType;
import com.balsamic.sejongmalsami.object.constants.Faculty;
import com.balsamic.sejongmalsami.object.constants.QuestionPresetTag;
import com.balsamic.sejongmalsami.object.constants.SortType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@Builder
@Getter
@Setter
@ToString
@AllArgsConstructor
public class QuestionCommand {
  // 2024.11.15 : SUHSAECHAN : 페이지 기본갑 성정
  public QuestionCommand() {
    this.pageNumber = 0;
    this.pageSize = 30;
  }

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
  private Integer pageNumber;
  @Schema(defaultValue = "30")
  private Integer pageSize;

  // 핕터링 파라미터
  private Faculty faculty; // 단과대별 조회 (필터링)
  private SortType sortType; // 정렬 타입 (최신순, 좋아요순, 엽전현상금순, 조회순)
  private ChaetaekStatus chaetaekStatus; // 채택여부 (전체, 채갵, 미채택)

}
