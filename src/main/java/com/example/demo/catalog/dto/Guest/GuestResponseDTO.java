package com.example.demo.catalog.dto.Guest;

import lombok.Data;

@Data
public class GuestResponseDTO {
    private Long id;
    private String name;
    private String email;
    private String phone;
}