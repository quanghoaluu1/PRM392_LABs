package com.example.se183138.present;

import com.example.se183138.model.AuthModel;
import com.example.se183138.view.SignupView;

public class SignupPresenter {
    private SignupView view;
    private AuthModel model;

    public SignupPresenter(SignupView view) {
        this.view = view;
        this.model = new AuthModel();
    }

    public void performSignup( String email,String username, String password, String confirmPassword) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            view.showSignupError("All fields are required");
            return;
        }
        if (!password.equals(confirmPassword)) {
            view.showSignupError("Passwords do not match");
            return;
        }
        model.signup(email, password, username, new AuthModel.AuthCallBack() {
            @Override
            public void onSuccess(String message) {
                view.showSignupSuccess(message);
                view.navigateToLogin();
            }

            @Override
            public void onFailure(String error) {
                view.showSignupError(error);
            }
        });
    }

    public void onLoginClicked() {
        view.navigateToLogin();
    }
}
