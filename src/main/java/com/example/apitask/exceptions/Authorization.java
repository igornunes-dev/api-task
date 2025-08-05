package com.example.apitask.exceptions;

public class Authorization extends RuntimeException {
    public Authorization(String message) {
        super(message);
    }
}
