package com.tutorialregister.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @ManyToOne
    private Student student;

    @ManyToOne
    private Staff staff;

    @NotBlank
    @Column(nullable = false, length = 160)
    private String title;

    @NotBlank
    @Column(nullable = false, length = 1000)
    private String message;

    @Column(length = 40)
    private String channel;

    private LocalDateTime scheduledAt;

    private LocalDateTime sentAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.DRAFT;
}
