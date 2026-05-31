package com.tutorialregister.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "staff")
public class Staff extends BaseEntity {

    @NotBlank
    @Column(nullable = false, length = 120)
    private String fullName;

    @Email
    @Column(unique = true, length = 160)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(length = 120)
    private String designation;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Gender gender;

    private LocalDate joinedDate;

    @OneToOne
    private UserAccount userAccount;

    @jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @jakarta.persistence.JoinColumn(name = "institution_id")
    private Institution institution;

    /** Courses this staff member teaches (inverse side of teacher foreign key). */
    @OneToMany(mappedBy = "teacher", fetch = FetchType.LAZY)
    private List<Course> teachingCourses = new ArrayList<>();
}
