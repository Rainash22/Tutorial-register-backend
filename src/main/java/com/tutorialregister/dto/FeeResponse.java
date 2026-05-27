package com.tutorialregister.dto;

import com.tutorialregister.model.FeeStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FeeResponse(
    Long id,
    StudentSummaryResponse student,
    CourseSummaryResponse course,
    BigDecimal totalFee,
    BigDecimal outstandingAmount,
    BigDecimal amountPaid,
    LocalDate dueDate,
    FeeStatus status,
    String remarks,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
