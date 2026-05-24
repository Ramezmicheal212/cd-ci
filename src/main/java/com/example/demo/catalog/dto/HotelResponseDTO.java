package com.example.demo.catalog.dto;

import java.util.List;

public record HotelResponseDTO(
            Long id,
            String name,
            String address,
            String city,
            String country,
            String description,
            String imageUrl,
            List<String> imageUrls
                ) {


}
