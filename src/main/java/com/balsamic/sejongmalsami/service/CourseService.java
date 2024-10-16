package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.postgres.Course;
import com.balsamic.sejongmalsami.object.CourseCommand;
import com.balsamic.sejongmalsami.object.CourseDto;
import com.balsamic.sejongmalsami.object.constants.Faculty;
import com.balsamic.sejongmalsami.repository.postgres.CourseRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

  private final CourseRepository courseRepository;

  @Async
  @Transactional
  public void parseAndSaveCourses(CourseCommand command) {
    MultipartFile sejongCourseFile = command.getSejongCourseFile();
    String fileName = sejongCourseFile.getOriginalFilename();
    Integer year = null;
    Integer semester = null;

    // 파일 포맷 체크 (예시 : course-2024-2.xlsx )
    String[] parts = Objects.requireNonNull(fileName).split("-");
    if (!Objects.equals(parts[0], "course")) {
      log.info("교과목명 파싱 파일 : {}", sejongCourseFile.getOriginalFilename());
      throw new CustomException(ErrorCode.WRONG_COURSE_FILE_FORMAT);
    }
    try {
      log.info("교과목명 파싱 파일 : {}", sejongCourseFile.getOriginalFilename());
      year = Integer.parseInt(parts[1]);
      semester = Integer.parseInt(parts[2].split("\\.")[0]);
    } catch (Exception e) {
      e.printStackTrace();
      log.info("교과목명 파싱 파일 : {}", sejongCourseFile.getOriginalFilename());
      throw new CustomException(ErrorCode.WRONG_COURSE_FILE_FORMAT);
    }

    // 중복 파일 업로드 확인
    if (courseRepository.existsByYearAndSemester(year, semester)) {
      log.info("교과목명 중복됨 : 년도: {} , 학기: {}", command.getYear(), command.getSemester());
      throw new CustomException(ErrorCode.DUPLICATE_COURSE_UPLOAD);
    }

    // MultipartFile을 InputStream으로 변환
    try (InputStream inputStream = sejongCourseFile.getInputStream()) {
      log.info("교과목명 파일 InputStream 생성 성공");
      Workbook workbook = new XSSFWorkbook(inputStream);  // InputStream을 사용하여 Workbook 생성
      Sheet sheet = workbook.getSheetAt(0);  // 첫 번째 시트를 가져온다

      for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) { // 첫 번째 행은 헤더이므로 1부터 시작한다
        Row row = sheet.getRow(i);

        String facultyName = row.getCell(0).getStringCellValue(); // 단과대학명
        String department = row.getCell(1).getStringCellValue(); // 학과명
        String subject = row.getCell(2).getStringCellValue(); // 교과목명

        Faculty faculty;
        // 단과대학명이 "대학"이라고 적힌 경우, "법학부 법학전공"으로 처리
        if (facultyName.equals("대학")) {
          faculty = Faculty.법학부법학전공;
        } else {
          try {
            faculty = Faculty.valueOf(facultyName);  // 해당 enum을 찾는다
          } catch (IllegalArgumentException e) {
            log.error("잘못된 단과대학명: {}", facultyName);
            throw new CustomException(ErrorCode.WRONG_FACULTY_NAME);
          }
        }

        // Course 객체 생성 및 저장
        Course course = Course.builder()
            .faculty(faculty)
            .department(department)
            .subject(subject)
            .year(year)
            .semester(semester)
            .build();

        log.info("교과목명 저장됨: faculty: {}, department: {}, subject: {}", faculty, department, subject);
        courseRepository.save(course); // DB에 저장
      }
      workbook.close();
    } catch (IOException e) {
      log.error("교과목명 파일 InputStream 생성 중 오류 발생", e);
      throw new CustomException(ErrorCode.COURSE_SAVE_ERROR);
    }
    log.info("교과목명 파싱 완료됨: {}", sejongCourseFile.getOriginalFilename());
  }

  // 특정 단과대학에 해당하는 교과목명 목록을 반환하는 메서드
  public CourseDto getSubjectsByFaculty(CourseCommand command) {
    return CourseDto.builder()
        .subjects(courseRepository.findDistinctSubjectByFaculty(command.getFaculty()))
        .build();
  }
}
