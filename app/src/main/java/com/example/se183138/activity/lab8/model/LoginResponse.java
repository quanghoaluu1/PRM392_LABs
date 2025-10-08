package com.example.se183138.activity.lab8.model;

public class LoginResponse {
    private String status;
    private String message;
    private User user;

    public static class User {
        private String username;
        private String avatar;

        public String getUsername() { return username; }
        public String getAvatar() { return avatar; }
    }



    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

}
