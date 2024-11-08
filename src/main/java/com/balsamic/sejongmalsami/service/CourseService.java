package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.postgres.Course;
import com.balsamic.sejongmalsami.object.CourseCommand;
import com.balsamic.sejongmalsami.object.CourseDto;
import com.balsamic.sejongmalsami.object.constants.Faculty;
import com.balsamic.sejongmalsami.repository.postgres.CourseRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

  private final CourseRepository courseRepository;

  @Transactional
  public int parseAndSaveCourses(MultipartFile multipartFile) {
    try {
      return parseAndSaveCourses(multipartFile.getOriginalFilename(), multipartFile.getInputStream());
    } catch (IOException e) {
      log.error("파일을 읽는 중 오류 발생: {}", multipartFile.getOriginalFilename(), e);
      throw new CustomException(ErrorCode.COURSE_SAVE_ERROR);
    }
  }

  @Transactional
  public int parseAndSaveCourses(File file) {
    try {
      return parseAndSaveCourses(file.getName(), Files.newInputStream(file.toPath()));
    } catch (IOException e) {
      log.error("파일을 읽는 중 오류 발생: {}", file.getName(), e);
      throw new CustomException(ErrorCode.COURSE_SAVE_ERROR);
    }
  }

  private int parseAndSaveCourses(String fileName, InputStream inputStream) {
    log.debug("Parsing courses from file: {}", fileName);
    Integer year;
    Integer semester;
    int addedCourses = 0;

    // 파일 포맷 체크 (예시 : course-2024-2.xlsx )
    String[] parts = Objects.requireNonNull(fileName).split("-");
    if (parts.length < 3 || !parts[0].equals("course")) {
      log.info("교과목명 파싱 파일 형식 오류: {}", fileName);
      throw new CustomException(ErrorCode.WRONG_COURSE_FILE_FORMAT);
    }

    try {
      log.info("교과목명 파싱 파일: {}", fileName);
      year = Integer.parseInt(parts[1]);
      semester = Integer.parseInt(parts[2].split("\\.")[0]);
    } catch (Exception e) {
      log.error("년도 또는 학기 추출 실패: {}", fileName, e);
      throw new CustomException(ErrorCode.WRONG_COURSE_FILE_FORMAT);
    }

    // 중복 파일 업로드 확인
    if (courseRepository.existsByYearAndSemester(year, semester)) {
      log.info("교과목명 중복됨: 년도: {} , 학기: {}", year, semester);
      throw new CustomException(ErrorCode.DUPLICATE_COURSE_UPLOAD);
    }

    // InputStream을 사용하여 Workbook 생성
    try (Workbook workbook = new XSSFWorkbook(inputStream)) {
      Sheet sheet = workbook.getSheetAt(0); // 첫 번째 시트를 가져온다
      log.debug("Workbook 열기 성공: {}", fileName);

      for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) { // 첫 번째 행은 헤더이므로 1부터 시작
        Row row = sheet.getRow(i);
        if (row == null) continue;

        String facultyName = row.getCell(0).getStringCellValue(); // 단과대학명
        String department = row.getCell(1).getStringCellValue(); // 학과명
        String subject = row.getCell(2).getStringCellValue(); // 교과목명

        log.debug("읽은 행: facultyName={}, department={}, subject={}", facultyName, department, subject);

        Faculty faculty;
        // 단과대학명이 "대학"이라고 적힌 경우, "법학부 법학전공"으로 처리
        if (facultyName.equals("대학")) {
          faculty = Faculty.법학부법학전공;
        } else {
          try {
            faculty = Faculty.valueOf(facultyName); // 해당 enum을 찾는다
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
        addedCourses++;
      }
      log.debug("교과목명 파싱 완료됨: {}", fileName);
    } catch (IOException e) {
      log.error("교과목명 파일 InputStream 생성 중 오류 발생: {}", fileName, e);
      throw new CustomException(ErrorCode.COURSE_SAVE_ERROR);
    }

    return addedCourses;
  }

  // 특정 단과대학에 해당하는 교과목명 목록을 반환하는 메서드
  public CourseDto getSubjectsByFaculty(CourseCommand command) {
    return CourseDto.builder()
        .subjects(courseRepository.findDistinctSubjectByFaculty(command.getFaculty()))
        .build();
  }

  @Transactional
  public void deleteCoursesByYearAndSemester(Integer year, Integer semester) {
    courseRepository.deleteByYearAndSemester(year, semester);
    log.info("년도 {} 학기 {}의 모든 교과목 삭제됨", year, semester);
  }
}
