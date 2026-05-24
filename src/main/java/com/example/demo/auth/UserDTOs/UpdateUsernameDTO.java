package com.example.demo.auth.UserDTOs;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateUsernameDTO(
    @NotBlank(message = "New username cannot be blank")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    String newUsername
) {
    
}
