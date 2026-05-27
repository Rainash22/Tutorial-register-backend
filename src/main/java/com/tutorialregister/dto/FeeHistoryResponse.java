package com.tutorialregister.dto;

import com.tutorialregister.model.FeeHistoryStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FeeHistoryResponse(
    Long id,
    /** Parent fee identifier — use GET /api/fees/{feeId} for full fee details. */
    Long feeId,
    StudentSummaryResponse student,
    CourseSummaryResponse course,
    BigDecimal amountPaid,
    LocalDate paidDate,
    String paymentReference,
    FeeHistoryStatus historyStatus,
    String remarks,
    LocalDateTime createdAt
) {
}
