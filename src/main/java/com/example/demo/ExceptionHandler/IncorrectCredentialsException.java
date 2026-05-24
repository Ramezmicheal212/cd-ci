package com.example.demo.ExceptionHandler;

public class IncorrectCredentialsException  extends RuntimeException {
    public IncorrectCredentialsException() {
        super("Incorrect Credentials");
    }
    
}
