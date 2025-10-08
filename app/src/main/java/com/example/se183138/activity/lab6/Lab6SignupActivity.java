package com.example.se183138.activity.lab6;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.se183138.R;
import com.example.se183138.present.SignupPresenter;
import com.example.se183138.view.SignupView;

public class Lab6SignupActivity extends AppCompatActivity implements SignupView {
    EditText edtEmail, edtUsername, edtPassword, edtConfirmPassword;
    Button btnLogin, btnSignup;
    SignupPresenter signupPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lab6_signup);

        edtEmail = findViewById(R.id.edtEmail);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);

        signupPresenter = new SignupPresenter(this);

        btnSignup.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String username = edtUsername.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();
            String confirmPassword = edtConfirmPassword.getText().toString().trim();
            Log.d("DEBUG", "Email: " + email + ", Username: " + username + ", Password: " + password + ", Confirm Password: " + confirmPassword);

            signupPresenter.performSignup(email, username, password, confirmPassword);
        });

        btnLogin.setOnClickListener(v -> signupPresenter.onLoginClicked());
    }

    @Override
    public void showSignupSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSignupError(String error) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void navigateToLogin() {
        Intent intent = new Intent(this, Lab6LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
