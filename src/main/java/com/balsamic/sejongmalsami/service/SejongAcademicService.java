package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.SejongAcademicCommand;
import com.balsamic.sejongmalsami.object.SejongAcademicDto;
import com.balsamic.sejongmalsami.object.postgres.Faculty;
import com.balsamic.sejongmalsami.repository.postgres.FacultyRepository;
import com.balsamic.sejongmalsami.repository.postgres.SubjectRepository;
import com.balsamic.sejongmalsami.util.log.LogUtil;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SejongAcademicService {
  private final FacultyRepository facultyRepository;
  private final SubjectRepository subjectRepository;
  /**
   * https://namu.wiki/w/%EC%84%B8%EC%A2%85%EB%8C%80%ED%95%99%EA%B5%90/%ED%95%99%EB%B6%80
   * //TODO: yml 파일에 놓는 방법 고려 필요
   *
   * 나무위키 공식 정보
   * 2024.12.10 : ACTIVE_FACULTY_NAMES 업데이트
   */
  private static final List<String> ACTIVE_FACULTIES = Arrays.asList(
      "인문과학대학",
      "사회과학대학",
      "경영경제대학",
      "호텔관광대학",
      "자연과학대학",
      "생명과학대학",
      "인공지능융합대학",
      "공과대학",
      "예체능대학",
      "대양휴머니티칼리지"
  );

  /**
   * https://namu.wiki/w/%EB%B6%84%EB%A5%98:%EC%84%B8%EC%A2%85%EB%8C%80%ED%95%99%EA%B5%90/%ED%95%99%EB%B6%80
   * //TODO: yml 파일에 놓는 방법 고려 필요
   *
   * 나무위키 공식 정보
   * 2024.12.10 : ACTIVE_DEPARTMENTS 업데이트
   */
  public static final List<String> ACTIVE_DEPARTMENTS = Arrays.asList(
      "국어국문학과",
      "영어영문학전공",
      "일어일문학전공",
      "중국통상학전공",
      "역사학과",
      "교육학과",
      "한국언어문화전공",
      "국제통상전공",
      "국제협력전공",
      "행정학과",
      "미디어커뮤니케이션학과",
      "법학과",
      "경영학부",
      "경제학과",
      "호텔관광경영학전공",
      "외식경영학전공",
      "호텔외식관광프랜차이즈경영학과",
      "글로벌조리학과",
      "수학통계학과",
      "물리천문학과",
      "화학과",
      "식품생명공학전공",
      "바이오융합공학전공",
      "바이오산업자원공학전공",
      "스마트생명산업융합학과",
      "전자정보통신공학과",
      "반도체시스템공학과",
      "컴퓨터공학과",
      "정보보호학과",
      "소프트웨어학과",
      "인공지능데이터사이언스학과",
      "AI로봇학과",
      "디자인이노베이션전공",
      "만화애니메이션텍전공",
      "건축공학과",
      "건축학과",
      "건설환경공학과",
      "환경에너지공간융합학과",
      "지구자원시스템공학과",
      "기계공학과",
      "우주항공공학전공",
      "항공시스템공학전공",
      "지능형드론융합전공",
      "나노신소재공학과",
      "양자원자력공학과",
      "국방시스템공학과",
      "회화과",
      "패션디자인학과",
      "음악과",
      "체육학과",
      "무용과",
      "영화예술학과"
  );

  @Cacheable(value = "faculties")
  public SejongAcademicDto getAllFaculties(SejongAcademicCommand command) {
    List<Faculty> faculties = facultyRepository.findByIsActiveTrue();
    return SejongAcademicDto.builder()
        .faculties(faculties)
        .build();
  }

  public void processFacultyIsActive() {
    LogUtil.lineLog("단과대(Faculty) 정보 업데이트 시작 : " + LocalDateTime.now().toString());
    List<Faculty> allFaculties = facultyRepository.findAll();
    // 나무위키 정보 -> isActive 판단
    for (Faculty faculty : allFaculties) {
      if (ACTIVE_FACULTIES.contains(faculty.getName())) {
        faculty.setIsActive(true);
      } else {
        faculty.setIsActive(false);
      }
    }
    facultyRepository.saveAll(allFaculties);
    LogUtil.lineLog("단과대(Faculty) 정보 업데이트 완료 : " + LocalDateTime.now().toString());
  }

  @Cacheable(value = "subjects")
  public SejongAcademicDto getDistinctSubjectNames() {
    List<String> subjectNames = subjectRepository.findDistinctSubjectNames();
    return SejongAcademicDto.builder()
        .subjects(subjectNames)
        .build();
  }
}
