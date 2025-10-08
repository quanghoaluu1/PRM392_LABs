package com.example.se183138.activity.lab4;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.se183138.R;

public class Lab4InstagramActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.lab4_instagram);

        TextView tv = findViewById(R.id.tv_login_facebook);
        ImageView iv = findViewById(R.id.facebook_logo);

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Lab4InstagramActivity.this, Lab4FacebookActivity.class);
                startActivity(intent);
            }
        });

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Lab4InstagramActivity.this, Lab4FacebookActivity.class);
                startActivity(intent);
            }
        });


    }
}
