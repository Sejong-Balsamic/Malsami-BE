package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.Course;
import com.balsamic.sejongmalsami.object.CourseCommand;
import com.balsamic.sejongmalsami.object.CourseDto;
import com.balsamic.sejongmalsami.object.constants.Faculty;
import com.balsamic.sejongmalsami.repository.CourseRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

  private final CourseRepository courseRepository;

  @Async
  public void parseAndSaveCourses(CourseCommand command){
    MultipartFile sejongCourseFile = command.getSejongCourseFile();

    log.info("교과목명 파싱 파일 : {}", sejongCourseFile.getOriginalFilename());

    // MultipartFile을 InputStream으로 변환
    try (InputStream inputStream = sejongCourseFile.getInputStream()) {
      Workbook workbook = new XSSFWorkbook(inputStream);  // InputStream을 사용하여 Workbook 생성
      Sheet sheet = workbook.getSheetAt(0);  // 첫 번째 시트를 가져온다

      for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) { // 첫 번째 행은 헤더이므로 1부터 시작한다
        Row row = sheet.getRow(i);

        Faculty faculty = Faculty.valueOf(row.getCell(0).getStringCellValue()); // 단과대학
        String department = row.getCell(1).getStringCellValue(); // 학과
        String subject = row.getCell(2).getStringCellValue(); // 교과목명

        // 법학부 법학전공은 "대학" 이라고 EXCEL에 표기되어 있습니다
        if(department.equals("법학부 법학전공")){
          faculty = Faculty.법학부법학전공;
        }
        // Course 객체를 생성하고 데이터를 저장한다
        Course course = Course.builder()
            .faculty(faculty)
            .department(department)
            .subject(subject)
            .build();

        log.info("교과목명 저장됨 : faculty: {}, department: {}, subject: {}", faculty, department, subject);

        // 단과대학별 교과목명 저장
        courseRepository.save(course);
      }
      workbook.close();
    } catch (IOException e) {
      throw new CustomException(ErrorCode.COURSE_SAVE_ERROR);
    }
  }

  // 특정 단과대학에 해당하는 교과목명 목록을 반환하는 메서드
  public CourseDto getSubjectsByFaculty(CourseCommand command) {
    return CourseDto.builder()
        .subjects(courseRepository.findDistinctSubjectByFaculty(command.getFaculty()))
        .build();
  }
}
