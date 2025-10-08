package com.example.se183138.activity.lab8;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.se183138.R;
import com.example.se183138.activity.lab8.model.LoginResponse;
import com.example.se183138.api.ApiResponse;
import com.example.se183138.api.ApiService;
import com.example.se183138.api.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Màn hình đăng nhập (Lab 8).
 * - Gửi yêu cầu đăng nhập tới server, lưu tuỳ chọn "Ghi nhớ"
 * - Điều hướng sang màn hình chào mừng khi thành công
 * - Kiểm tra phiên đăng nhập hiện tại (session) khi khởi chạy
 */
public class Lab8LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private EditText inputUsername, inputPassword;
    Button buttonLogin, buttonSignup, buttonForgotPassword;
    private CheckBox cbRememberMe;
    private SharedPreferences prefs;
    ApiService apiService;

    /** Khởi tạo UI, gán sự kiện và kiểm tra session trên server. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lab8_login);

        // Khởi tạo các thành phần giao diện

        inputUsername = findViewById(R.id.edtUsername);
        inputPassword = findViewById(R.id.edtPassword);
        buttonLogin = findViewById(R.id.btnLogin);
        buttonSignup = findViewById(R.id.btnSignup);
        buttonForgotPassword = findViewById(R.id.btnForgotPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

        checkServerSession();
        // Thiết lập sự kiện nhấn nút
        buttonLogin.setOnClickListener(v -> login());
        buttonSignup.setOnClickListener(v -> startActivity(new Intent(Lab8LoginActivity.this, Lab8SignupActivity.class)));
        buttonForgotPassword.setOnClickListener(v -> startActivity(new Intent(Lab8LoginActivity.this, Lab8ForgotPasswordActivity.class)));
    }

    /** Gọi API đăng nhập với tham số từ giao diện và xử lý phản hồi. */
    private void login() {
        // Lấy giá trị từ các trường nhập liệu
        final String username = inputUsername.getText().toString().trim();
        final String password = inputPassword.getText().toString().trim();
        boolean remember = cbRememberMe.isChecked();

        // Kiểm tra thông tin người dùng
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Chua nhap du 2 thong tin username va password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Log các tham số gửi đi
        Log.d(TAG, "Sending parameters: action=login, acc_kh=" + username + ", pass_kh=" + password + ", remember=" + remember);

        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);

        // Gọi API
        apiService.login("login", username, password, remember).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    String status = loginResponse.getStatus();
                    String message = loginResponse.getMessage();

                    Log.d(TAG, "Response: " + response.body()); // Log phản hồi từ server

                    if (status.equals("success")) {

                        LoginResponse.User user = loginResponse.getUser();
                        String username = user.getUsername();
                        String avatar = user.getAvatar();
                        prefs.edit().putBoolean("remember", remember)
                                .putString("username", username)
                                .putString("avatar", avatar)
                                .apply();
                        Intent intent = new Intent(Lab8LoginActivity.this, Lab8WelcomeActivity.class);
                        intent.putExtra("username", username);
                        intent.putExtra("avatar", avatar);
                        Toast.makeText(Lab8LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(Lab8LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Response error: " + response.message());
                    Toast.makeText(Lab8LoginActivity.this, "Response error", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                Toast.makeText(Lab8LoginActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Kiểm tra phiên đăng nhập hợp lệ từ server để tự động đăng nhập. */
    private void checkServerSession() {
        apiService = RetrofitClient.getRetrofitInstance(this).create(ApiService.class);
        apiService.checkSession("check_session").enqueue(new Callback<LoginResponse>() {

            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getStatus().equals("success")) {
                        LoginResponse loginResponse = response.body();
                        LoginResponse.User user = loginResponse.getUser();
                        String username = user.getUsername();
                        String avatar = user.getAvatar();
                        Intent intent = new Intent(Lab8LoginActivity.this, Lab8WelcomeActivity.class);
                        intent.putExtra("username", username);
                        intent.putExtra("avatar", avatar);
                        startActivity(intent);
                        finish();
                    } else {
                        prefs.edit().clear().apply();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("SessionCheck", "Network error: " + t.getMessage());
            }
        });
    }
}
