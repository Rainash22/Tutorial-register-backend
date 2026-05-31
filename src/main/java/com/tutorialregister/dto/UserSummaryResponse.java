package com.tutorialregister.dto;

public record UserSummaryResponse(
    Long id,
    String username,
    String email,
    String generatedPassword
) {
}
