package com.example.se183138;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.se183138.ui.login.Lab5MainActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button_lab1).setOnClickListener(v -> {
            startActivity(new Intent(this, Lab1Activity.class));
        });
        findViewById(R.id.button_lab2).setOnClickListener(v -> {
            startActivity(new Intent(this, Lab2Activity.class));
        });
        findViewById(R.id.button_lab3).setOnClickListener(v -> {
            startActivity(new Intent(this, Lab3Activity.class));
        });
        findViewById(R.id.button_lab4).setOnClickListener(v -> {
            startActivity(new Intent(this, Lab4InstagramActivity.class));
        });
        findViewById(R.id.button_lab5).setOnClickListener(v -> {
            startActivity(new Intent(this, Lab5MainActivity.class));
        });
    }

}