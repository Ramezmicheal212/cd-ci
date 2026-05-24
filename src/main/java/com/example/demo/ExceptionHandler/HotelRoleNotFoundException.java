package com.example.demo.ExceptionHandler;

public class HotelRoleNotFoundException extends RuntimeException {

    public HotelRoleNotFoundException(Long id) {
        super("Hotel role with id " + id + " not found");
    }

    public HotelRoleNotFoundException(String name) {
        super("Hotel role with name " + name + " not found");
    }
}
