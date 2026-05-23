package com.tutorialregister.dto;

import com.tutorialregister.model.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record StaffRequest(
    @NotBlank String fullName,
    @Email String email,
    String phone,
    String designation,
    Gender gender,
    LocalDate joinedDate,
    Long userAccountId
) {
}
