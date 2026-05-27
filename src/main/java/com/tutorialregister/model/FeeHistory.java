package com.tutorialregister.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * Audit record of every payment installment made against a {@link Fee}.
 * Stored in the {@code fee_history} table.
 */
@Getter
@Setter
@Entity
@Table(name = "fee_history")
public class FeeHistory extends BaseEntity {

    /** The parent fee record this installment belongs to. */
    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Fee fee;

    /**
     * Denormalized student reference for fast audit queries
     * without joining through the fee record.
     */
    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Student student;

    /**
     * Denormalized course reference for fast audit queries
     * without joining through the fee record.
     */
    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Course course;

    /** Amount received in this installment. Must be positive. */
    @NotNull
    @Positive
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amountPaid;

    /** Date on which this installment was received. */
    private LocalDate paidDate;

    /** Bank / payment gateway transaction reference. */
    @Column(length = 120)
    private String paymentReference;

    /** Type of this history entry: PAYMENT, ADJUSTMENT, REFUND, WAIVER. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeeHistoryStatus historyStatus = FeeHistoryStatus.PAYMENT;

    @Column(length = 500)
    private String remarks;
}
