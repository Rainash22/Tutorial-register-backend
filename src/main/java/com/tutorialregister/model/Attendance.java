package com.tutorialregister.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "attendance",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_attendance_student_course_date",
        columnNames = {"student_id", "course_id", "attendance_date"}
    )
)
public class Attendance extends BaseEntity {

    @NotNull
    @ManyToOne(optional = false)
    private Student student;

    /** The course this attendance record belongs to (may be null for ad-hoc records). */
    @ManyToOne
    private Course course;

    @ManyToOne
    private Staff markedBy;

    @NotNull
    @Column(nullable = false)
    private LocalDate attendanceDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    @Column(length = 500)
    private String remarks;
}
