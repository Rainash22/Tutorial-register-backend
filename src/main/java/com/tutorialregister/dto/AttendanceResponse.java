package com.tutorialregister.dto;

import com.tutorialregister.model.AttendanceStatus;
import java.time.LocalDate;

public record AttendanceResponse(
    Long id,
    StudentSummaryResponse student,
    StaffSummaryResponse markedBy,
    LocalDate attendanceDate,
    AttendanceStatus status,
    String remarks
) {
}
