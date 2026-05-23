package com.tutorialregister.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "students")
public class Student extends BaseEntity {

    @NotBlank
    @Column(nullable = false, length = 120)
    private String fullName;

    @Column(unique = true, length = 40)
    private String admissionNumber;

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    @Column(length = 80)
    private String className;

    @Column(length = 120)
    private String courseName;

    @Column(length = 120)
    private String guardianName;

    @Column(length = 30)
    private String guardianPhone;

    @Email
    @Column(length = 160)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 500)
    private String address;

    private LocalDate admissionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudentStatus status = StudentStatus.ACTIVE;

    @ManyToOne
    private Staff assignedStaff;

    @OneToOne
    private UserAccount userAccount;
}
