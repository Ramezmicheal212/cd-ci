package com.example.demo.auth.UserDTOs;

import jakarta.validation.constraints.NotBlank;

public record AssignRoleToUSerDTO(
    @NotBlank String roleId
) {
    
}
