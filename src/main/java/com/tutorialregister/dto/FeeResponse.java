package com.tutorialregister.dto;

import com.tutorialregister.model.FeeStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record FeeResponse(
    Long id,
    StudentSummaryResponse student,
    BigDecimal amountDue,
    BigDecimal amountPaid,
    LocalDate dueDate,
    LocalDate paidDate,
    FeeStatus status,
    String paymentReference,
    String remarks
) {
}
