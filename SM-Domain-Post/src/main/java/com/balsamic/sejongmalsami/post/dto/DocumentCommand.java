package com.balsamic.sejongmalsami.post.dto;

import com.balsamic.sejongmalsami.constants.ContentType;
import com.balsamic.sejongmalsami.constants.DocumentType;
import com.balsamic.sejongmalsami.constants.LikeType;
import com.balsamic.sejongmalsami.constants.PostTier;
import com.balsamic.sejongmalsami.constants.SortType;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@ToString
@AllArgsConstructor
@Getter
@Setter
@Builder
public class DocumentCommand {
  // 2024.11.15 : SUHSAECHAN : 페이지 기본값 설정
  public DocumentCommand() {
    this.pageNumber = 0;
    this.pageSize = 30;
    this.attachmentFiles = new ArrayList<>();
  }

  private UUID memberId; // 자료, 자료 요청
  private String title; // 자료, 자료 요청
  private String content; // 자료, 자료 요청
  private String subject; // 자료
  private String faculty; // 자료 요청
  private List<DocumentType> documentTypes; // 자료, 자료 요청
  private List<MultipartFile> attachmentFiles; // 첨부된 파일들
  private Integer attendedYear;
  private Boolean isDepartmentPrivate; // 자료
  private Boolean isPrivate; // 자료 요청
  private List<String> customTags; // 커스텀 태그

  private UUID documentPostId;
  @Schema(hidden = true, description = "회원")
  @JsonIgnore
  private Member member;

  @Schema(defaultValue = "0")
  private Integer pageNumber;
  @Schema(defaultValue = "30")
  private Integer pageSize;
  private ContentType contentType;
  private LikeType likeType; // 글 좋아요/싫어요
  private SortType sortType; // 최신순, 좋아요순
  private PostTier postTier;

  @Schema(defaultValue = "/projects/sejong-malsami/document/DOCUMENT_sejong-malsami-test_b482d4cc-722c-42e7-b042-b4a3d5903ae5.mp4")
  private String filePath;
  private UUID documentFileId;
}
