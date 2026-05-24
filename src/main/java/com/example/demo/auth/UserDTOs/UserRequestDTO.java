package com.example.demo.auth.UserDTOs;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


public record UserRequestDTO(
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores, and must be at least 10 characters long")
    String username, 
    @NotBlank
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", message = "Password must be at least 8 characters long and contain both letters and numbers")
    String password,
    @NotBlank
    String fullName, 
    @Email(message = "Email should be valid")
    @NotBlank
    String email,
    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Phone number must be between 7 and 15 digits, and can start with +")
     String phoneNumber) {
    
}
