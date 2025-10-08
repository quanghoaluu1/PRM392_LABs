package com.example.se183138.activity.lab2;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.se183138.R;

public class Lab2Activity extends AppCompatActivity {
    private int count = 0;
    private boolean isCounting = false;

    private Button buttonCount;
    private Button buttonReset;
    private Button buttonToast;

    private TextView textCount;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.lab2);

        buttonCount = findViewById(R.id.button_count);
        buttonReset = findViewById(R.id.button_reset);
        buttonToast = findViewById(R.id.button_toast);
        textCount = findViewById(R.id.show_count);

        buttonCount.setOnClickListener(v -> {
            if (!isCounting){
                isCounting = true;
                startCounting();
            }else{
                isCounting = false;
            }

        });

        buttonReset.setOnClickListener(v -> {
            isCounting = false;
            count = 0;
            textCount.setText(String.valueOf(count));
        });
        buttonToast.setOnClickListener(v -> {
            isCounting = false;
            Toast.makeText(this, "This is toast", Toast.LENGTH_LONG).show();
        });

    }

    private void startCounting() {
        isCounting = true;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isCounting) {
                    count++;
                    textCount.setText(String.valueOf(count));
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }
}
