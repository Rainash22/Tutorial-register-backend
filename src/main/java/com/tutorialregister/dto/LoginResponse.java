package com.tutorialregister.dto;

public record LoginResponse(
    String token,
    String username,
    String email,
    String roles
) {}
