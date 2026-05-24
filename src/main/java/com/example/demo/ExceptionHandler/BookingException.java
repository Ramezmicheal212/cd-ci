package com.example.demo.ExceptionHandler;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// ✅ NEW FILE — BookingService was importing from com.booking.exception which doesn't exist in your project
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BookingException extends RuntimeException {
    public BookingException(String message) {
        super(message);
    }
}