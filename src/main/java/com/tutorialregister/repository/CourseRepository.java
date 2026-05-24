package com.tutorialregister.repository;

import com.tutorialregister.model.Course;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByTeacherId(Long teacherId);

    List<Course> findByStudentsId(Long studentId);
}
