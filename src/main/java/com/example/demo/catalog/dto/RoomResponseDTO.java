package com.example.demo.catalog.dto;

import java.math.BigDecimal;
import java.util.Set;

public record RoomResponseDTO(

        Long id,
        String roomType,
        Integer capacity,
        BigDecimal basePrice,
        Set<String> amenities, 
        Long hotelId,
        String imageUrl

) {
}