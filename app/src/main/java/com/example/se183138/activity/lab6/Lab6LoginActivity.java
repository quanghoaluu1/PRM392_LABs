package com.example.se183138.activity.lab6;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.se183138.R;
import com.example.se183138.present.LoginPresenter;
import com.example.se183138.view.LoginView;

public class Lab6LoginActivity extends AppCompatActivity implements LoginView {
    EditText edtEmail, edtPassword;
    Button btnLogin, btnSignup;
    LoginPresenter loginPresenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lab6_login);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);

        loginPresenter = new LoginPresenter(this);

        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            loginPresenter.performLogin(email, password);
        });

        btnSignup.setOnClickListener(v -> loginPresenter.onSignupClicked());

    }

    @Override
    public void showLoginSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showLoginError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToSignup() {
        startActivity(new Intent(this, Lab6SignupActivity.class));
        finish();
    }

    @Override
    public void navigateToWelcome() {
        startActivity(new Intent(this, Lab6WelcomeActivity.class));
        finish();
    }
}
