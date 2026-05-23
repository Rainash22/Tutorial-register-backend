package com.tutorialregister.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

    @NotBlank
    @Column(nullable = false, unique = true, length = 60)
    private String name;

    @Column(length = 255)
    private String description;
}
