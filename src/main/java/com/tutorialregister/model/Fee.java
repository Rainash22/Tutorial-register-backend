package com.tutorialregister.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "fees")
public class Fee extends BaseEntity {

    @NotNull
    @ManyToOne(optional = false)
    private Student student;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amountDue;

    @PositiveOrZero
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    private LocalDate dueDate;

    private LocalDate paidDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeeStatus status = FeeStatus.PENDING;

    @Column(length = 120)
    private String paymentReference;

    @Column(length = 500)
    private String remarks;
}
