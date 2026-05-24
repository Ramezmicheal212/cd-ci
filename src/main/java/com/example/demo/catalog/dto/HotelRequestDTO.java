package com.example.demo.catalog.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.NotBlank;
public record HotelRequestDTO(

        @NotBlank(message = "Hotel name is required")
            String name,
            @NotBlank(message = "Hotel address is required")
                String address,
                @NotBlank(message = "Hotel city is required")
                String city,
                @NotBlank(message = "Hotel country is required")
                String country,
                String description,
                MultipartFile[] hotelImages) {


}
    