package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.SejongAcademicCommand;
import com.balsamic.sejongmalsami.object.SejongAcademicDto;
import com.balsamic.sejongmalsami.object.postgres.Faculty;
import com.balsamic.sejongmalsami.repository.postgres.FacultyRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

@Service
@RequiredArgsConstructor
public class SejongAcademicService {
  private final FacultyRepository facultyRepository;

  @Cacheable(value = "faculties")
  public SejongAcademicDto getAllFaculties(SejongAcademicCommand command) {
    List<Faculty> faculties = facultyRepository.findAll();
    return SejongAcademicDto.builder()
        .faculties(faculties)
        .build();
  }
}
