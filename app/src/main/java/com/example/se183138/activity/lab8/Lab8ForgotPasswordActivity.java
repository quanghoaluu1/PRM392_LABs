package com.example.se183138.activity.lab8;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.se183138.R;
import com.example.se183138.api.ApiResponse;
import com.example.se183138.api.ApiService;
import com.example.se183138.api.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Lab8ForgotPasswordActivity extends AppCompatActivity {
    private static final String TAG = "ForgotPasswordActivity";

    private EditText inputUsername, inputNewPassword, inputConfirmPassword;
    private Button buttonResetPassword;
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lab8_forgot_password);
        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        inputUsername = findViewById(R.id.edtUsername);
        inputNewPassword = findViewById(R.id.edtNewPassword);
        inputConfirmPassword = findViewById(R.id.edtConfirmPassword);
        buttonResetPassword = findViewById(R.id.btnResetPassword);

        buttonResetPassword.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword(){
        String username = inputUsername.getText().toString().trim();
        String newPassword = inputNewPassword.getText().toString().trim();
        String confirmPassword = inputConfirmPassword.getText().toString().trim();

        if (username.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        apiService.forgotPassword("forgotPassword", username, newPassword, confirmPassword).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                Log.d(TAG, "Request :" + call.request());
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    String status = apiResponse.getStatus();
                    String message = apiResponse.getMessage();

                    Log.d(TAG, "Response: " + response.body());

                    if (status.equals("success")) {
                        Toast.makeText(Lab8ForgotPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Lab8ForgotPasswordActivity.this, Lab8LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(Lab8ForgotPasswordActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Response error: " + response.message());
                    Toast.makeText(Lab8ForgotPasswordActivity.this, "Response error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                Toast.makeText(Lab8ForgotPasswordActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
