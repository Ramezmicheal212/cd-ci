package com.example.demo.auth.UserDTOs;

import java.util.Set;
import java.util.Map;



public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        String username,
        Set<String> authorities
        
        ) {

}
