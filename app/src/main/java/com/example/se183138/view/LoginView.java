package com.example.se183138.view;

public interface LoginView {
    void showLoginSuccess(String message);
    void showLoginError(String error);
    void navigateToSignup();
    void navigateToWelcome();
}
