package com.tutorialregister.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;

public record CourseRequest(
    @NotBlank String name,
    @NotBlank String code,
    String description,
    @PositiveOrZero BigDecimal courseFee,
    Integer maxStudents,
    Boolean isActive,
    Long teacherId,
    @Valid List<ClassScheduleRequest> schedules
) {
}
