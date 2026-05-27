package com.tutorialregister.dto;

/** Compact course summary embedded inside Fee and FeeHistory responses. */
public record FeeSummaryResponse(
    Long id,
    Long studentId,
    String studentName,
    Long courseId,
    String courseName,
    String courseCode
) {
}
