package com.example.se183138.activity.lab8;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.se183138.R;
import com.example.se183138.api.ApiResponse;
import com.example.se183138.api.ApiService;
import com.example.se183138.api.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Màn hình đăng ký tài khoản (Lab 8).
 * - Thu thập thông tin đăng ký và gọi API tạo tài khoản
 * - Điều hướng về màn hình đăng nhập sau khi đăng ký thành công
 */
public class Lab8SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    ApiService apiService;
    private EditText inputUsername, inputPassword, inputRepassword;

    /** Khởi tạo UI và gán sự kiện cho các nút. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lab8_signup);

        // Sử dụng ID đúng từ layout
        inputUsername = findViewById(R.id.edtUsername);
        inputPassword = findViewById(R.id.edtPassword);
        inputRepassword = findViewById(R.id.edtConfirmPassword);
        Button buttonSignup = findViewById(R.id.btnSignup); // Cập nhật ID của nút đăng ký
        Button buttonRelogin = findViewById(R.id.btnLogin); // Thêm nút "Đăng nhập lại"
        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);


        // Xử lý sự kiện khi nhấn nút đăng ký
        buttonSignup.setOnClickListener(v -> register());

        // Xử lý sự kiện khi nhấn nút "Đăng nhập lại"
        buttonRelogin.setOnClickListener(v -> {
            // Chuyển sang LoginActivity
            Intent intent = new Intent(Lab8SignupActivity.this, Lab8LoginActivity.class);
            startActivity(intent);
            finish(); // Đóng SignupActivity để tránh quay lại bằng nút back
        });
    }

    /** Gọi API đăng ký với dữ liệu người dùng và xử lý phản hồi. */
    private void register() {
        final String username = inputUsername.getText().toString().trim();
        final String password = inputPassword.getText().toString().trim();
        final String repassword = inputRepassword.getText().toString().trim();

        // Kiểm tra thông tin người dùng
        if (username.isEmpty() || password.isEmpty() || repassword.isEmpty()) {
            Toast.makeText(this, "Nhập tất các fields", Toast.LENGTH_SHORT).show();
            return;
        }


        // Gọi API đăng ký
        apiService.signup("register", username, password, repassword).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse> call, @NonNull Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    String status = apiResponse.getStatus();
                    String message = apiResponse.getMessage();

                    Log.d(TAG, "Response: " + response.body());

                    if (status.equals("success")) {
                        Toast.makeText(Lab8SignupActivity.this, message, Toast.LENGTH_SHORT).show();
                        // Sau khi đăng ký thành công, chuyển sang màn hình đăng nhập
                        startActivity(new Intent(Lab8SignupActivity.this, Lab8LoginActivity.class));
                        finish();
                    } else {
                        Toast.makeText(Lab8SignupActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Response error: " + response.message());
                    Toast.makeText(Lab8SignupActivity.this, "Response error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                Toast.makeText(Lab8SignupActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
