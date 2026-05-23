package com.tutorialregister.dto;

import com.tutorialregister.model.NotificationStatus;
import java.time.LocalDateTime;

public record NotificationResponse(
    Long id,
    StudentSummaryResponse student,
    StaffSummaryResponse staff,
    String title,
    String message,
    String channel,
    LocalDateTime scheduledAt,
    LocalDateTime sentAt,
    NotificationStatus status
) {
}
