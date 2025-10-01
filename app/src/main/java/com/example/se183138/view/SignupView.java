package com.example.se183138.view;

public interface SignupView {
    void showSignupSuccess(String message);
    void showSignupError(String error);
    void navigateToLogin();
}
