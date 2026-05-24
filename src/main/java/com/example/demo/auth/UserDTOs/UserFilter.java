package com.example.demo.auth.UserDTOs;

public record UserFilter(
        String username,
        String roleName,
        String hotelName,
        String email,
        String phoneNumber,
        Boolean enabled,
        String fullName
) {
    
}