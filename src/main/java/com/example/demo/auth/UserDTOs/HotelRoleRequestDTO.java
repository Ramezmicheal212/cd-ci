package com.example.demo.auth.UserDTOs;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record HotelRoleRequestDTO(
        @NotBlank
        String roleName,
        @NotEmpty
        Set<@NotBlank String> permissions
        ) {

}
