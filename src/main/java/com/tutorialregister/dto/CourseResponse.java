package com.tutorialregister.dto;

import java.math.BigDecimal;
import java.util.List;

public record CourseResponse(
    Long id,
    String name,
    String code,
    String description,
    BigDecimal courseFee,
    Integer maxStudents,
    Boolean isActive,
    StaffSummaryResponse teacher,
    List<ClassScheduleResponse> schedules,
    List<StudentSummaryResponse> students
) {
}
