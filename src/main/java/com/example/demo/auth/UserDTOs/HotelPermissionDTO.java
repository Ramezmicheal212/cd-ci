package com.example.demo.auth.UserDTOs;
import java.util.Set;

public record HotelPermissionDTO (
Long hotelId,

String hotelName,

String role,

Set<String> permissions
) {
    
}