package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.DocumentType;
import com.balsamic.sejongmalsami.object.postgres.Member;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@ToString
@Getter
@Setter
@NoArgsConstructor
public class DocumentCommand {

  private UUID memberId; // 자료, 자료 요청
  private String title; // 자료, 자료 요청
  private String content; // 자료, 자료 요청
  private String subject; // 자료, 자료 요청
  private Set<DocumentType> documentTypeSet; // 자료, 자료 요청
  private Boolean isDepartmentPrivate; // 자료
  private Boolean isPrivate; // 자료 요청

  private UUID documentPostId;
  private Member member;

  private Integer pageNumber;
  private Integer pageSize;
  private String sort; // 최신순, 좋아요순


  private List<MultipartFile> attachmentFiles = new ArrayList<>(); // 첨부된 파일들

  private List<MultipartFile> documentFiles = new ArrayList<>();
  private List<MultipartFile> imageFiles = new ArrayList<>();
  private List<MultipartFile> videoFiles = new ArrayList<>();
  private List<MultipartFile> musicFiles = new ArrayList<>();
}
