package com.example.demo.auth.UserDTOs;

import java.util.List;
import java.util.Set;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record RoleRequestDTO(
        String roleName,
        @NotEmpty Set<@NotBlank String> permissions
) {
}
