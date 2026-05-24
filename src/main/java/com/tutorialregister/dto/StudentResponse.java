package com.tutorialregister.dto;

import com.tutorialregister.model.Gender;
import com.tutorialregister.model.StudentStatus;
import java.time.LocalDate;
import java.util.List;

public record StudentResponse(
    Long id,
    String fullName,
    String admissionNumber,
    LocalDate dateOfBirth,
    Gender gender,
    String className,
    String guardianName,
    String guardianPhone,
    String email,
    String phone,
    String address,
    LocalDate admissionDate,
    StudentStatus status,
    StaffSummaryResponse assignedStaff,
    UserSummaryResponse userAccount,
    List<CourseSummaryResponse> enrolledCourses
) {
}
