package com.example.demo.catalog.dto;
import com.example.demo.auth.entity.EmploymentStatus;

public record EmployeeResponseDTO(
    Long id,
    String name,
    String email ,
    String roleName,
    EmploymentStatus status
) {
}