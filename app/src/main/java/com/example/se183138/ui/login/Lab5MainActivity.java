package com.example.se183138.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.se183138.R;
import com.example.se183138.data.UserRepository;

/**
 * Main login activity that handles user authentication
 * Provides interface for users to enter credentials and login to the app
 */
public class Lab5MainActivity extends AppCompatActivity {
    // UI component references for email and password input
    private EditText et_email, et_password;
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
            setContentView(R.layout.lab5_login);

            // Find and configure the "create account" text link
            TextView tv_create_account = findViewById(R.id.tv_create_account);
            tv_create_account.setOnClickListener(v -> {
                try {
                    // Navigate to registration screen when clicked
                    Intent intent = new Intent(Lab5MainActivity.this, Lab5SignUpActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Lỗi khi chuyển trang: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            // Initialize the database repository for user operations
            userRepository = new UserRepository(this);

            // Find and store references to input fields and buttons
            et_email = findViewById(R.id.emailOrPhone_text);
            et_password = findViewById(R.id.password_text);
            Button btn_login = findViewById(R.id.button_login);
            Button btn_go_register = findViewById(R.id.button_register);

            // Initially disable login button until both fields have content
            btn_login.setEnabled(false);

            // Create a text watcher to monitor input field changes
            TextWatcher textWatcher = new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    // Not used but required by interface
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // Not used but required by interface
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    try {
                        // Get current text from both input fields
                        String email = et_email.getText().toString().trim();
                        String password = et_password.getText().toString().trim();

                        // Enable login button only when both fields have content
                        btn_login.setEnabled(!email.isEmpty() && !password.isEmpty());
                    } catch (Exception e) {
                        // Handle any text change errors silently to avoid UI disruption
                    }
                }
            };

            // Attach text watchers to both input fields for real-time validation
            et_email.addTextChangedListener(textWatcher);
            et_password.addTextChangedListener(textWatcher);

            // Configure register button to navigate to sign-up screen
            btn_go_register.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(Lab5MainActivity.this, Lab5SignUpActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(this, "Lỗi khi chuyển trang: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            // Configure login button to attempt user authentication
            btn_login.setOnClickListener(v -> handleLogin());
        } catch (Exception e) {
            Toast.makeText(this, "Lỗi khởi tạo ứng dụng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handles the login process when user clicks the login button
     * Validates input fields and attempts authentication through UserRepository
     */
    private void handleLogin(){
        try {
            // Get trimmed text from input fields to remove whitespace
            String email = et_email.getText().toString().trim();
            String pass = et_password.getText().toString().trim();

            // Validate that email field is not empty
            if (email.isEmpty()) {
                Toast.makeText(this, "Email không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate that password field is not empty
            if (pass.isEmpty()) {
                Toast.makeText(this, "Password không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }

            // Attempt login through repository
            boolean success = userRepository.login(email, pass);
            if (success) {
                // Login successful - show success message and navigate to welcome screen
                Toast.makeText(Lab5MainActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Lab5MainActivity.this, Lab5WelcomeActivity.class);
                intent.putExtra("email", email); // Pass email to welcome screen
                startActivity(intent);
            } else {
                // Login failed - show error message
                Toast.makeText(Lab5MainActivity.this, "Sai email hoặc mật khẩu", android.widget.Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            // Handle any unexpected errors during login process
            Toast.makeText(this, "Lỗi đăng nhập: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
