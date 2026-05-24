package com.tutorialregister.dto;

import com.tutorialregister.model.Gender;
import java.time.LocalDate;
import java.util.List;

public record StaffResponse(
    Long id,
    String fullName,
    String email,
    String phone,
    String designation,
    Gender gender,
    LocalDate joinedDate,
    UserSummaryResponse userAccount,
    List<CourseSummaryResponse> teachingCourses
) {
}
