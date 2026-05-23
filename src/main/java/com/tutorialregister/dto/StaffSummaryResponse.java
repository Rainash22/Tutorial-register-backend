package com.tutorialregister.dto;

public record StaffSummaryResponse(
    Long id,
    String fullName,
    String designation
) {
}
