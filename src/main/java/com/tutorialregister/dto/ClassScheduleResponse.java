package com.tutorialregister.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record ClassScheduleResponse(
    Long id,
    DayOfWeek dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    String room
) {
}
