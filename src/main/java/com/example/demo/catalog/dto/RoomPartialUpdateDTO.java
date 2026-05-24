package com.example.demo.catalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.util.Set;

public record RoomPartialUpdateDTO(

        String roomType,

        @Min(value = 1, message = "Capacity must be at least 1")
        Integer capacity,

        @DecimalMin(value = "0.0", inclusive = false, message = "Base price must be greater than 0")
        BigDecimal basePrice,

        Set<Long> amenityIds

) {
}