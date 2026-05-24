package com.example.demo.auth.UserDTOs;
import java.util.Set;

public record AssignRoleRequestDTO(
      Set<String> roleNames
) {
    
}