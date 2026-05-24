package com.example.demo.auth.UserDTOs;

import java.util.List;

public record HotelRoleResponseDTO(Long id, String roleName, List<String> permissions) {

}
