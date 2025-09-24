package com.example.se183138.data.model;

public class LoggedInUser {
    private String email;
    private String password;
    private String repass;

    public LoggedInUser(String email, String password, String repass) {
        this.email = email;
        this.password = password;
        this.repass = repass;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
    public String getRepass() {
        return repass;
    }
}
