package com.tutorialregister.dto;

import com.tutorialregister.model.FeeHistoryStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request body for recording a payment installment against a Fee.
 * On successful creation the parent Fee's outstandingAmount is automatically reduced.
 */
public record FeeHistoryRequest(
    @NotNull Long feeId,
    @NotNull @Positive BigDecimal amountPaid,
    LocalDate paidDate,
    String paymentReference,
    FeeHistoryStatus historyStatus,
    String remarks
) {
}
