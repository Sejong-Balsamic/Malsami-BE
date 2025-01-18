package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.mongo.YeopjeonHistory;
import com.balsamic.sejongmalsami.object.postgres.Course;
import com.balsamic.sejongmalsami.object.postgres.CourseFile;
import com.balsamic.sejongmalsami.object.postgres.Faculty;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.object.postgres.ServerErrorCode;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
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
