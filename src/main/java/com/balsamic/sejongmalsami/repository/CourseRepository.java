package com.balsamic.sejongmalsami.repository;

import com.balsamic.sejongmalsami.object.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course,Long> {

}
