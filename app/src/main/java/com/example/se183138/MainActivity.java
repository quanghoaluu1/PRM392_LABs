package com.example.se183138;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.se183138.activity.lab1.Lab1Activity;
import com.example.se183138.activity.lab2.Lab2Activity;
import com.example.se183138.activity.lab3.Lab3Activity;
import com.example.se183138.activity.lab4.Lab4InstagramActivity;
import com.example.se183138.activity.lab6.Lab6LoginActivity;
import com.example.se183138.activity.lab5.Lab5MainActivity;
import com.example.se183138.activity.lab8.Lab8LoginActivity;

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
        findViewById(R.id.button_lab6).setOnClickListener(v -> {
            startActivity(new Intent(this, Lab6LoginActivity.class));
        });
        findViewById(R.id.button_lab7).setOnClickListener(v -> {
            startActivity(new Intent(this, Lab8LoginActivity.class));
        });
        }
    }
