package com.tutorialregister.dto;

import com.tutorialregister.model.Gender;
import com.tutorialregister.model.StudentStatus;
import java.time.LocalDate;

public record StudentResponse(
    Long id,
    String fullName,
    String admissionNumber,
    LocalDate dateOfBirth,
    Gender gender,
    String className,
    String courseName,
    String guardianName,
    String guardianPhone,
    String email,
    String phone,
    String address,
    LocalDate admissionDate,
    StudentStatus status,
    StaffSummaryResponse assignedStaff,
    UserSummaryResponse userAccount
) {
}
