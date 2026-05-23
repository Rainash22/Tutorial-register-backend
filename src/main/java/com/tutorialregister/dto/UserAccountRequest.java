package com.tutorialregister.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record UserAccountRequest(
    @NotBlank String username,
    @Email @NotBlank String email,
    @NotBlank String passwordHash,
    Boolean enabled,
    Set<Long> roleIds
) {
}
