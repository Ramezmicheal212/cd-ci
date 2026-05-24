package com.example.demo.auth.UserDTOs;
import java.util.List;

public record UpdateUsernameResponseDTO(
     Long id,
        String username,
        String accessToken
) {
    
}
