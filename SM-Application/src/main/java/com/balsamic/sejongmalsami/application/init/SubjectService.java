package com.balsamic.sejongmalsami.application.init;

import com.balsamic.sejongmalsami.object.postgres.Subject;
import com.balsamic.sejongmalsami.repository.postgres.SubjectRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubjectService {

  private final SubjectRepository subjectRepository;
  private final CourseService courseService;

  /**
   * 중복 없는 교과목명을 추출하여 Subject 엔티티에 저장합니다.
   */
  @Transactional
  public void processDistinctSubjects() {
    log.info("중복 없는 교과목명 처리 시작");
    List<String> distinctSubjects = courseService.getDistinctSubjects();

    int addedSubjects = 0;
    for (String subjectName : distinctSubjects) {
      if (!subjectRepository.existsByName(subjectName)) {
        Subject subject = Subject.builder()
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
        subjectRepository.save(subject);
        addedSubjects++;
        log.info("새로운 Subject 추가됨: {}", subjectName);
      } else {
        log.info("이미 존재하는 Subject: {}", subjectName);
      }
    }

    log.info("Subject 처리 완료: 추가된 교과목 수 = {}", addedSubjects);
  }
}
