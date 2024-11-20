package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.DocumentType;
import com.balsamic.sejongmalsami.object.constants.Faculty;
import com.balsamic.sejongmalsami.object.constants.SortType;
import com.balsamic.sejongmalsami.object.postgres.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@ToString
@Getter
@Setter
public class DocumentCommand {
  // 2024.11.15 : SUHSAECHAN : 페이지 기본값 설정
  public DocumentCommand() {
    this.pageNumber = 0;
    this.pageSize = 30;
  }

  private UUID memberId; // 자료, 자료 요청
  private String title; // 자료, 자료 요청
  private String content; // 자료, 자료 요청
  private String subject; // 자료
  private Faculty faculty; // 자료 요청
  private List<DocumentType> documentTypes; // 자료, 자료 요청
  private Boolean isDepartmentPrivate; // 자료
  private Boolean isPrivate; // 자료 요청

  private UUID documentPostId;
  private Member member;

  @Schema(defaultValue = "0")
  private Integer pageNumber;
  @Schema(defaultValue = "30")
  private Integer pageSize;
  private SortType sortType; // 최신순, 좋아요순


  private List<MultipartFile> attachmentFiles = new ArrayList<>(); // 첨부된 파일들

  private List<MultipartFile> documentFiles = new ArrayList<>();
  private List<MultipartFile> imageFiles = new ArrayList<>();
  private List<MultipartFile> videoFiles = new ArrayList<>();
  private List<MultipartFile> musicFiles = new ArrayList<>();
}
