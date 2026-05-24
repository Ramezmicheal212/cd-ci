package com.example.demo.auth.UserDTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePasswordDTO(
        String currentPassword,
        @NotBlank(message = "New password must not be blank")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String newPassword,
        @NotBlank(message = "Confirm password must not be blank")
        String confirmPassword
) {
}
