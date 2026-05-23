package com.tutorialregister.dto;

public record StudentSummaryResponse(
    Long id,
    String fullName,
    String admissionNumber
) {
}
