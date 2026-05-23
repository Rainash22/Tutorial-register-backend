package com.tutorialregister.dto;

import com.tutorialregister.model.AssessmentType;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AssessmentResponse(
    Long id,
    StudentSummaryResponse student,
    StaffSummaryResponse evaluatedBy,
    String title,
    AssessmentType type,
    BigDecimal maxMarks,
    BigDecimal marksObtained,
    LocalDate assessmentDate,
    String remarks
) {
}
