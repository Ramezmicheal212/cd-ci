package com.example.demo.catalog.dto;


import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public record HotelPartialUpdateDTO(
            String name,
            String address,
            String city,
            String country,
            String description,
            
            MultipartFile[]  hotelImages
    ) {

}
