package com.example.se183138;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.se183138.model.User;
import com.example.se183138.view.WelcomeView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Lab6WelcomeActivity extends AppCompatActivity implements WelcomeView {
    TextView txtWelcome;
    private FirebaseAuth mAuth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lab6_welcome);

        txtWelcome = findViewById(R.id.txtWelcome);
        mAuth = FirebaseAuth.getInstance();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance("https://prm392firebase-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");

        String uid = mAuth.getCurrentUser().getUid();
        mDatabase.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user == null) {
                    return;
                }
                Log.d("Lab6WelcomeActivity", "Username: " + user.getUsername());
               displayUserInfo(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void displayUserInfo(String username) {
        txtWelcome.setText("Welcome, " + username + "!");
    }
}
