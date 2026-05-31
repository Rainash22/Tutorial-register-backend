package com.tutorialregister.repository;

import com.tutorialregister.model.Course;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByTeacherId(Long teacherId);

    List<Course> findByStudentsId(Long studentId);

    List<Course> findByTeacherUserAccountUsername(String username);

    List<Course> findByStudentsUserAccountUsername(String username);

    List<Course> findByInstitutionId(Long institutionId);

    List<Course> findByInstitutionIdAndTeacherUserAccountUsername(Long institutionId, String username);

    List<Course> findByInstitutionIdAndStudentsUserAccountUsername(Long institutionId, String username);
}
