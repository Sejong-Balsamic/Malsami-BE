package com.balsamic.sejongmalsami.application.dto;

import com.balsamic.sejongmalsami.dto.MemberYeopjeon;
import com.balsamic.sejongmalsami.mongo.YeopjeonHistory;
import com.balsamic.sejongmalsami.postgres.Course;
import com.balsamic.sejongmalsami.postgres.CourseFile;
import com.balsamic.sejongmalsami.postgres.Faculty;
import com.balsamic.sejongmalsami.postgres.Member;
import com.balsamic.sejongmalsami.postgres.ServerErrorCode;
import com.balsamic.sejongmalsami.postgres.Yeopjeon;
import com.balsamic.sejongmalsami.postgres.QuestionPost;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Page;

@Builder
@Getter
@Setter
@ToString
public class AdminDto {

  private Member member;
  private Page<MemberYeopjeon> memberYeopjeonPage;
  private Yeopjeon yeopjeon;
  private YeopjeonHistory yeopjeonHistory;
  private List<Faculty> faculties;
  private List<String> subjects;
  private List<Integer> years;
  private List<Integer> semesters;
  private String fileName;
  private String filePath;
  private byte[] fileBytes;

  private Page<Course> coursePage;
  private Page<CourseFile> courseFilePage;
  private Page<ServerErrorCode> serverErrorCodesPage;
   private Page<QuestionPost> questionPostPage;
}
