package com.tutorialregister.dto;

import com.tutorialregister.model.Gender;
import com.tutorialregister.model.StudentStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record StudentRequest(
    @NotBlank String fullName,
    String admissionNumber,
    LocalDate dateOfBirth,
    Gender gender,
    String className,
    String courseName,
    String guardianName,
    String guardianPhone,
    @Email String email,
    String phone,
    String address,
    LocalDate admissionDate,
    StudentStatus status,
    Long assignedStaffId,
    Long userAccountId
) {
}
