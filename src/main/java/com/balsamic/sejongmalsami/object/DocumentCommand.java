package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.DocumentType;
import com.balsamic.sejongmalsami.object.constants.UploadType;
import com.balsamic.sejongmalsami.object.postgres.Member;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@ToString
@Getter
@Setter
@Builder
public class DocumentCommand {

  private UUID memberId; // 자료, 자료 요청
  private String title; // 자료, 자료 요청
  private String content; // 자료, 자료 요청
  private String subject; // 자료, 자료 요청
  private Set<DocumentType> documentTypeSet; // 자료, 자료 요청
  private Boolean isDepartmentPrivate; // 자료
  private Boolean isPrivate; // 자료 요청

  private UUID postId;
  private Member member;
  private MultipartFile targetUploadFile;
  private MultipartFile targetThumbnailFile;
  private UploadType uploadType;

  private List<MultipartFile> attachmentFiles; // 첨부된 파일들 (업로드전)
  private List<MultipartFile> documentFiles; // 문서
  private List<MultipartFile> imageFiles; // 이미지
  private List<MultipartFile> videoFiles; // 비디오
  private List<MultipartFile> musicFiles; // 음원
}
