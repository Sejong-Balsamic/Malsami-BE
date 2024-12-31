package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.postgres.Member;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@ToString
public class AdminCommand {

  public AdminCommand() {
    this.pageNumber = 0;
    this.pageSize = 30;
  }

  private Member member;
  private Integer amount;
  private UUID targetMemberId;

  // 회원
  private Long studentId;
  private String studentName;
  private UUID memberId;
  private String memberIdStr;
  private String uuidNickname;

  // 과목
  private String subject;
  private String faculty;
  private Integer year;
  private Integer semester;
  private MultipartFile multipartFile;

  private Integer pageNumber;
  private Integer pageSize;
  private String sortField;
  private String sortDirection;
}
