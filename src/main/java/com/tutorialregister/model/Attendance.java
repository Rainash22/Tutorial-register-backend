package com.tutorialregister.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "attendance")
public class Attendance extends BaseEntity {

    @NotNull
    @ManyToOne(optional = false)
    private Student student;

    @ManyToOne
    private Staff markedBy;

    @NotNull
    @Column(nullable = false)
    private LocalDate attendanceDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    @Column(length = 500)
    private String remarks;
}
