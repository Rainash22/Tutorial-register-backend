package com.tutorialregister.repository;

import com.tutorialregister.model.Attendance;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    /** Returns true if an attendance record already exists for the same student, course and date. */
    boolean existsByStudentIdAndCourseIdAndAttendanceDate(
        Long studentId, Long courseId, LocalDate attendanceDate
    );
}
