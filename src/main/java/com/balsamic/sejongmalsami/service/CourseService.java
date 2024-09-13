package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.Course;
import com.balsamic.sejongmalsami.object.constants.Faculty;
import com.balsamic.sejongmalsami.repository.CourseRepository;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class CourseService {

  private final CourseRepository courseRepository;

  public CourseService(CourseRepository courseRepository) {
    this.courseRepository = courseRepository;
  }

  private Map<String, List<String>> facultySubjectMap = new HashMap<>();

  // 파일명을 통해 resources 밑에 있는 엑셀 파일을 읽는 메서드
  public void saveCoursesFromExcel(String fileName) throws IOException {
    ClassPathResource resource = new ClassPathResource("files/" + fileName);
    InputStream inputStream = resource.getInputStream();
    Workbook workbook = new XSSFWorkbook(inputStream);
    Sheet sheet = workbook.getSheetAt(0);  // 첫 번째 시트를 가져온다

    for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) { // 첫 번째 행은 헤더이므로 1부터 시작한다
      Row row = sheet.getRow(i);

      Faculty faculty = Faculty.valueOf(row.getCell(0).getStringCellValue()); // 단과대학
      String department = row.getCell(1).getStringCellValue(); // 학과
      String subject = row.getCell(2).getStringCellValue(); // 교과목명

      // Course 객체를 생성하고 데이터를 저장한다
      Course course = new Course();
      course.setFaculty(faculty);
      course.setDepartment(department);
      course.setSubject(subject);

      courseRepository.save(course);

      // 단과대학별 교과목명 저장한다
      facultySubjectMap.computeIfAbsent(String.valueOf(faculty), k -> new ArrayList<>()).add(subject);
    }

    workbook.close();
    inputStream.close();
  }

  // 특정 단과대학에 해당하는 교과목명 목록을 반환하는 메서드
  public List<String> getSubjectsByFaculty(String faculty) {
    return facultySubjectMap.getOrDefault(faculty, Collections.emptyList());
  }
}
