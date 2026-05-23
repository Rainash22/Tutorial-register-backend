package com.tutorialregister.dto;

import com.tutorialregister.model.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AttendanceRequest(
    @NotNull Long studentId,
    Long markedById,
    @NotNull LocalDate attendanceDate,
    @NotNull AttendanceStatus status,
    String remarks
) {
}
