package com.tutorialregister.dto;

import com.tutorialregister.model.NotificationStatus;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public record NotificationRequest(
    Long studentId,
    Long staffId,
    @NotBlank String title,
    @NotBlank String message,
    String channel,
    LocalDateTime scheduledAt,
    LocalDateTime sentAt,
    NotificationStatus status
) {
}
