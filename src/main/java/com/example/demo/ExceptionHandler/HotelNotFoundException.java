package com.example.demo.ExceptionHandler;

public class HotelNotFoundException extends RuntimeException {
    public HotelNotFoundException(Long id) {
        super("Hotel with ID " + id + " not found");
      
    }
}
