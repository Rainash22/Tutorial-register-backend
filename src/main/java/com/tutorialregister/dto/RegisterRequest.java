package com.tutorialregister.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 80, message = "Username must be between 3 and 80 characters")
    String username,

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password
) {}
