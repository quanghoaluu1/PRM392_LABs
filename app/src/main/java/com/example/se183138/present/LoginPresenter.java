package com.example.se183138.present;

import com.example.se183138.model.AuthModel;
import com.example.se183138.view.LoginView;

public class LoginPresenter {
    private LoginView view;
    private AuthModel model;

    public LoginPresenter(LoginView view) {
        this.view = view;
        this.model = new AuthModel();
    }

    public void performLogin(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            view.showLoginError("Email and password cannot be empty");
            return;
        }
        model.login(email, password, new AuthModel.AuthCallBack() {
            @Override
            public void onSuccess(String message) {
                view.showLoginSuccess(message);
                view.navigateToWelcome();
            }

            @Override
            public void onFailure(String error) {
                view.showLoginError(error);
            }
        });
    }

    public void onSignupClicked() {
        view.navigateToSignup();
    }
}
