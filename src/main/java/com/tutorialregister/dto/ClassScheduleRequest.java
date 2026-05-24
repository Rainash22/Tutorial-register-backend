package com.tutorialregister.dto;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;

public record ClassScheduleRequest(
    @NotNull DayOfWeek dayOfWeek,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime,
    String room
) {
}
