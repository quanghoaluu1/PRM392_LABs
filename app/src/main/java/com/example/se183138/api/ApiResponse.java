package com.example.se183138.api;

public class ApiResponse {
    private String status;
    private String message;

    public ApiResponse(String message, String status) {
        this.message = message;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}

