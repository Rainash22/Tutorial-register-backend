package com.tutorialregister.dto;

import com.tutorialregister.model.AssessmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

public record AssessmentRequest(
    @NotNull Long studentId,
    Long evaluatedById,
    @NotBlank String title,
    AssessmentType type,
    @NotNull @PositiveOrZero BigDecimal maxMarks,
    @PositiveOrZero BigDecimal marksObtained,
    LocalDate assessmentDate,
    String remarks
) {
}
