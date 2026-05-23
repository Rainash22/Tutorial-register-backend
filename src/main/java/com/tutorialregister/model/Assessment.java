package com.tutorialregister.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "assessments")
public class Assessment extends BaseEntity {

    @NotNull
    @ManyToOne(optional = false)
    private Student student;

    @ManyToOne
    private Staff evaluatedBy;

    @NotBlank
    @Column(nullable = false, length = 160)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssessmentType type = AssessmentType.TEST;

    @PositiveOrZero
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal maxMarks;

    @PositiveOrZero
    @Column(precision = 8, scale = 2)
    private BigDecimal marksObtained;

    private LocalDate assessmentDate;

    @Column(length = 500)
    private String remarks;
}
