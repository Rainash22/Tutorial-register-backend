package com.tutorialregister.repository;

import com.tutorialregister.model.Attendance;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    /** Returns true if an attendance record already exists for the same student, course and date. */
    boolean existsByStudentIdAndCourseIdAndAttendanceDate(
        Long studentId, Long courseId, LocalDate attendanceDate
    );

    List<Attendance> findByCourseTeachersUserAccountUsername(String username);

    List<Attendance> findByStudentUserAccountUsername(String username);

    List<Attendance> findByCourseInstitutionId(Long institutionId);

    List<Attendance> findByCourseInstitutionIdAndCourseTeachersUserAccountUsername(Long institutionId, String username);
}
