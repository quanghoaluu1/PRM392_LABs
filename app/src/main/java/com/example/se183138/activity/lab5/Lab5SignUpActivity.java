package com.example.se183138.activity.lab5;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.se183138.R;
import com.example.se183138.data.UserRepository;
import com.example.se183138.data.model.LoggedInUser;

/**
 * Registration activity that handles new user sign-up process
 * Provides interface for users to create new accounts with email validation
 */
public class Lab5SignUpActivity extends AppCompatActivity {
    // UI component references for registration form inputs
    private EditText et_email, et_pass, et_repass;
    // Repository instance for database operations
    private UserRepository userRepository;

    /**
     * Called when the activity is first created
     * Sets up the UI, initializes components, and configures event listeners
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            // Enable edge-to-edge display for modern UI
            EdgeToEdge.enable(this);
            // Set the layout for this activity
            setContentView(R.layout.lab5_register);

            // Find navigation elements to return to login screen
            TextView tv_login = findViewById(R.id.tv_to_login);
            Button btn_to_login = findViewById(R.id.button_to_login);

            // Initialize the database repository for user operations
            userRepository = new UserRepository(this);

            // Configure text link to navigate back to login screen
            tv_login.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Lab5SignUpActivity.this, Lab5MainActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(Lab5SignUpActivity.this, "Lỗi khi chuyển trang: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            // Configure button to navigate back to login screen
            btn_to_login.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Lab5SignUpActivity.this, Lab5MainActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(Lab5SignUpActivity.this, "Lỗi khi chuyển trang: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            // Find and store references to registration form input fields
            et_email = findViewById(R.id.emailOrPhone_text);
            et_pass = findViewById(R.id.password_text);
            et_repass = findViewById(R.id.password_confirmed_text);
            Button btn_register = findViewById(R.id.button_register);

            // Configure register button to handle user registration
            btn_register.setOnClickListener(v -> {
                try {
                    // Get trimmed text from all input fields
                    String email = et_email.getText().toString().trim();
                    String pass = et_pass.getText().toString().trim();
                    String repass = et_repass.getText().toString().trim();

                    // Validate email format and availability
                    if (!validateEmail(email)) {
                        return; // Stop if email validation fails
                    }

                    // Validate password requirements and confirmation
                    if (!validatePasswordAndRepassword(pass, repass)){
                        return; // Stop if password validation fails
                    }

                    // Create new user object with validated data
                    LoggedInUser newUser = new LoggedInUser(email, pass, repass);

                    // Attempt to register user in database
                    boolean isSuccess = userRepository.register(newUser);
                    if (isSuccess){
                        // Registration successful - show success message and return to login
                        Toast.makeText(this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(Lab5SignUpActivity.this, Lab5MainActivity.class);
                        startActivity(intent);
                        finish(); // Close this activity
                    } else {
                        // Registration failed - show error message
                        Toast.makeText(this, "Đăng ký thất bại!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    // Handle any unexpected errors during registration process
                    Toast.makeText(this, "Lỗi đăng ký: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khởi tạo ứng dụng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Validates email format and checks if email already exists in database
     * @param email Email address to validate
     * @return true if email is valid and available, false otherwise
     */
    private boolean validateEmail(String email) {
        // Check if email field is empty
        if (email.isEmpty()) {
            Toast.makeText(this, "Email không được để trống", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if email format is valid using Android's built-in pattern matcher
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if email already exists in database to prevent duplicates
        if (userRepository.isEmailExits(email)) {
            Toast.makeText(this, "Email đã tồn tại", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true; // Email is valid and available
    }

    /**
     * Validates password requirements and confirms password match
     * @param pass Primary password entered by user
     * @param repass Password confirmation entered by user
     * @return true if passwords meet requirements and match, false otherwise
     */
    private boolean validatePasswordAndRepassword(String pass, String repass){
        // Check if password field is empty
        if (pass.isEmpty()) {
            Toast.makeText(this, "Password không được để trống", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if password confirmation field is empty
        if (repass.isEmpty()) {
            Toast.makeText(this, "Repassword không được để trống", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if password meets minimum length requirement (6 characters)
        if (pass.length() < 6) {
            Toast.makeText(this, "Password phải ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if password and confirmation match
        if (!pass.equals(repass)) {
            Toast.makeText(this, "Mật khẩu không khớp!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true; // All password validations passed
    }

    /**
     * Called when the activity is being destroyed
     * Clean up resources, particularly the database connection
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            // Close database connection to prevent memory leaks
            if (userRepository != null) {
                userRepository.close();
            }
        } catch (Exception ignored) {
            // Ignore errors during cleanup to avoid disrupting app shutdown
        }
    }
}
