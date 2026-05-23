package com.tutorialregister.dto;

import java.util.List;

public record UserAccountResponse(
    Long id,
    String username,
    String email,
    boolean enabled,
    List<RoleResponse> roles
) {
}
