package com.tutorialregister.dto;

import com.tutorialregister.model.FeeStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;

public record FeeRequest(
    @NotNull Long studentId,
    @NotNull @PositiveOrZero BigDecimal amountDue,
    @PositiveOrZero BigDecimal amountPaid,
    LocalDate dueDate,
    LocalDate paidDate,
    FeeStatus status,
    String paymentReference,
    String remarks
) {
}
