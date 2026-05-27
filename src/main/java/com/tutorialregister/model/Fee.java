package com.tutorialregister.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "fees")
public class Fee extends BaseEntity {

    /** The student this fee belongs to. */
    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Student student;

    /** The course this fee was raised for (set at enrolment time). */
    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Course course;

    /** Original course fee captured at the time of enrolment. */
    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalFee = BigDecimal.ZERO;

    /** Remaining balance — starts equal to totalFee and decreases with each payment. */
    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal outstandingAmount = BigDecimal.ZERO;

    /** Cumulative amount received across all FeeHistory installments. */
    @PositiveOrZero
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeeStatus status = FeeStatus.PENDING;

    @Column(length = 500)
    private String remarks;

    /** Audit trail of every payment installment made against this fee. */
    @OneToMany(mappedBy = "fee", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeeHistory> history = new ArrayList<>();
}
