package com.example.demo.auth.UserDTOs;

import java.util.List;
import java.util.Set;

public record UserResponseDTO(
        Long id,
        String username,
        Set<String> roleNames,
        List<Long> hotelIds
) {
}
