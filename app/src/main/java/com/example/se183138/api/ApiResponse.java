package com.example.se183138.api;

public class ApiResponse {
    private String status;
    private String message;
    private String avatar_url;

    public ApiResponse(String message, String status, String avatar_url) {
        this.message = message;
        this.status = status;
        this.avatar_url = avatar_url;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getAvatar_url(){
        return avatar_url;
    }
}

