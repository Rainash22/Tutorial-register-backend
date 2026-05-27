package com.tutorialregister.repository;

import com.tutorialregister.model.Fee;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeeRepository extends JpaRepository<Fee, Long> {

    List<Fee> findByStudentId(Long studentId);

    List<Fee> findByCourseId(Long courseId);

    /** Used to check whether a fee already exists for a student-course pair (e.g., on re-enrolment). */
    Optional<Fee> findByStudentIdAndCourseId(Long studentId, Long courseId);
}
