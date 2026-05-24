package com.example.demo.catalog.dto;
import jakarta.validation.constraints.NotBlank;

public record AmenityRequestDTO(
    @NotBlank(message = "Amenity name cannot be null")
    String name,
    
    String description
) {

}
