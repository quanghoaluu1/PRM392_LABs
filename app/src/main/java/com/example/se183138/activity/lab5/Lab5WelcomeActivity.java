package com.example.se183138.activity.lab5;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.se183138.R;

/**
 * Welcome activity that displays a personalized greeting after successful login
 * Shows the user's email address in a welcome message
 */
public class Lab5WelcomeActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created
     * Sets up the UI and displays personalized welcome message with user's email
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            // Enable edge-to-edge display for modern UI
            EdgeToEdge.enable(this);
            // Set the layout for this activity
            setContentView(R.layout.lab5_welcome);

            // Find the TextView that will display the welcome message
            TextView tv_email = findViewById(R.id.tv_email);

            // Get the email passed from the previous activity (login screen)
            String email = getIntent().getStringExtra("email");

            if (email != null){
                // If email was provided, create personalized welcome message
                // Uses string resource with placeholder for proper internationalization
                tv_email.setText(getString(R.string.welcome_format, email));
            } else {
                // If no email provided (fallback case), show generic welcome message
                // Uses string resource for proper internationalization
                tv_email.setText(getString(R.string.welcome_user));
            }
        } catch (Exception e) {
            // Handle any unexpected errors during activity initialization
            Toast.makeText(this, "Lỗi khởi tạo trang chào mừng: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
