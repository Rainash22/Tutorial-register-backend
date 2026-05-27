package com.tutorialregister.dto;

import com.tutorialregister.model.FeeStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request body for manually creating or updating a Fee record.
 * When a student is enrolled via the course endpoint, the fee is created
 * automatically — this DTO is used only for admin overrides.
 */
public record FeeRequest(
    @NotNull Long studentId,
    @NotNull Long courseId,
    /** Override for the fee amount; if null on create, defaults to course.courseFee. */
    @PositiveOrZero BigDecimal totalFee,
    LocalDate dueDate,
    FeeStatus status,
    String remarks
) {
}
