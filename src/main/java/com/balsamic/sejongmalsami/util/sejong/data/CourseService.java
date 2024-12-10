package com.balsamic.sejongmalsami.util.sejong.data;

import com.balsamic.sejongmalsami.object.postgres.Course;
import com.balsamic.sejongmalsami.object.postgres.Subject;
import com.balsamic.sejongmalsami.repository.postgres.CourseRepository;
import com.balsamic.sejongmalsami.repository.postgres.DepartmentRepository;
import com.balsamic.sejongmalsami.repository.postgres.FacultyRepository;
import com.balsamic.sejongmalsami.repository.postgres.SubjectRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

  private final CourseRepository courseRepository;
  private final FacultyRepository facultyRepository;
  private final DepartmentRepository departmentRepository;
  private final SubjectRepository subjectRepository;

  // 매핑을 위한 상수 정의
  private static final String DEFAULT_FACULTY_NAME = "사회과학대학";
  private static final String SPECIAL_FACULTY_MAPPING = "대학"; // 실제는 사회과학대학
  private static final String SPECIAL_DEPARTMENT_NAME = "법학부 법학전공"; // 예외 처리할 법학부 법학전공 학과명

  // 예외 매핑을 위한 Map (추후 확장 가능)
  private static final Map<String, String> SPECIAL_DEPARTMENT_MAPPINGS = Map.of(
      "법학부 법학전공", "법학과"
      // 필요한 경우 추가 예외 매핑을 여기에 추가할 수 있습니다.
  );

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
    log.debug("교과목 파일 내부 파싱 시작 : {}", fileName);
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

        String facultyName = row.getCell(0).getStringCellValue().trim(); // 단과대학명
        String departmentName = row.getCell(1).getStringCellValue().trim(); // 학과명
        String subjectName = row.getCell(2).getStringCellValue().trim(); // 교과목명

        log.debug("읽은 행: facultyName={}, departmentName={}, subject={}", facultyName, departmentName, subjectName);

        // 예외처리: "대학"인 경우 "사회과학대학"으로 매핑
        if (SPECIAL_FACULTY_MAPPING.equals(facultyName)) {
          log.info("단과대학명이 '{}'이므로 '{}'으로 매핑합니다.", facultyName, DEFAULT_FACULTY_NAME);
          facultyName = DEFAULT_FACULTY_NAME;
        }

        // 예외 매핑 적용
        if (SPECIAL_DEPARTMENT_MAPPINGS.containsKey(departmentName)) {
          log.info("특별 매핑 적용: '{}'을 '{}'으로 매핑합니다.", departmentName, SPECIAL_DEPARTMENT_MAPPINGS.get(departmentName));
          departmentName = SPECIAL_DEPARTMENT_MAPPINGS.get(departmentName);
        }

        // Faculty 유효성 검사
        boolean facultyExists = facultyRepository.findByName(facultyName).isPresent();
        if (!facultyExists) {
          throw new CustomException(ErrorCode.FACULTY_NOT_FOUND);
        }

        // Subject 조회 또는 생성
        Subject subject = subjectRepository.findByName(subjectName)
            .orElseGet(() -> {
              Subject newSubject = Subject.builder()
                  .name(subjectName)
                  .dailyDocumentScore(0L)
                  .weeklyDocumentScore(0L)
                  .monthlyDocumentScore(0L)
                  .totalDocumentScore(0L)
                  .dailyQuestionScore(0L)
                  .weeklyQuestionScore(0L)
                  .monthlyQuestionScore(0L)
                  .totalQuestionScore(0L)
                  .build();
              log.info("새로운 Subject 추가됨: {}", subjectName);
              return subjectRepository.save(newSubject);
            });

        // Course 객체 생성 및 저장
        Course course = Course.builder()
            .faculty(facultyName) // String으로 설정
            .department(departmentName)
            .subject(subjectName)
            .year(year)
            .semester(semester)
            .build();

        log.info("교과목명 저장됨: faculty: {}, department: {}, subject: {}", facultyName, departmentName, subjectName);
        courseRepository.save(course); // DB에 저장
        addedCourses++;
      }
      log.debug("교과목명 파싱 완료됨: {}", fileName);
    } catch (IOException e) {
      log.error("교과목명 파일 InputStream 생성 중 오류 발생: {}", fileName, e);
      throw new CustomException(ErrorCode.COURSE_SAVE_ERROR);
    } catch (CustomException e) {
      // 예외가 발생하면 추가 처리할 수 있음 (예: 롤백)
      throw e;
    }

    return addedCourses;
  }

  @Transactional
  public void deleteCoursesByYearAndSemester(Integer year, Integer semester) {
    courseRepository.deleteByYearAndSemester(year, semester);
    log.info("{}년도  {}학기 의 모든 교과목 삭제됨", year, semester);
  }

  /**
   * Course -> 중복 없는 교과목명 목록을 조회합니다.
   */
  @Transactional(readOnly = true)
  public List<String> getDistinctSubjects() {
    // 모든 Course 엔티티에서 중복 없는 subject 이름을 추출
    return courseRepository.findAll()
        .stream()
        .map(Course::getSubject)
        .distinct()
        .collect(Collectors.toList());
  }
}
