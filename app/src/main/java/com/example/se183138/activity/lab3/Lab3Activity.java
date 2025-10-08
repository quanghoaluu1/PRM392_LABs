package com.example.se183138.activity.lab3;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.se183138.R;

public class Lab3Activity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvInput;

    private String input = "0";
    private String operator = null;
    private double firstOperand = 0;
    private boolean isNewInput = true;

    private final int[] numberButtonIds = {
            R.id.button_0, R.id.button_1, R.id.button_2, R.id.button_3, R.id.button_4,
            R.id.button_5, R.id.button_6, R.id.button_7, R.id.button_8, R.id.button_9
    };

    private final int[] operatorButtonIds = {
            R.id.button_plus,
            R.id.button_minus,
            R.id.button_multiply,
            R.id.button_divide
    };

    private final int[] specialButtonIds = {
            R.id.button_c,
            R.id.button_equal,
            R.id.button_backspace,
            R.id.button_plus_minus,
            R.id.button_dot,
            R.id.button_percent
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.lab3);

        tvInput = findViewById(R.id.tv_input);
        tvInput.setText(input);

        for (int id : numberButtonIds) findViewById(id).setOnClickListener(this);
        for (int id : operatorButtonIds) findViewById(id).setOnClickListener(this);
        for (int id : specialButtonIds) findViewById(id).setOnClickListener(this);
    }

    private double calculate(double a, double b, String op) {
        switch (op) {
            case "+": return a + b;
            case "-": return a - b;
            case "*": return a * b;
            case "/":
                if (b == 0) {
                    Toast.makeText(this, "Không thể chia cho 0", Toast.LENGTH_SHORT).show();
                    return 0;
                }
                return a / b;
            default: return b;
        }
    }

    private String formatNumber(double d) {
        if (d == Math.rint(d)) {
            return String.valueOf((long) d);
        }
        return String.valueOf(d);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        for (int btnId : numberButtonIds) {
            if (id == btnId) {
                String digit = ((Button) v).getText().toString();
                if (isNewInput || "0".equals(input)) {
                    input = digit;
                } else {
                    input += digit;
                }
                tvInput.setText(input);
                isNewInput = false;
                return;
            }
        }

        for (int btnId : operatorButtonIds) {
            if (id == btnId) {
                firstOperand = Double.parseDouble(input);
                operator = ((Button) v).getText().toString();
                isNewInput = true;
                return;
            }
        }

        if (id == R.id.button_c) {
            input = "0";
            operator = null;
            firstOperand = 0;
            isNewInput = true;
            tvInput.setText(input);

        } else if (id == R.id.button_equal) {
            if (operator != null) {
                double secondOperand = Double.parseDouble(input);
                double result = calculate(firstOperand, secondOperand, operator);
                input = formatNumber(result);
                tvInput.setText(input);

                firstOperand = result;
                operator = null;
                isNewInput = true;
            }

        } else if (id == R.id.button_backspace) {
            if (!isNewInput && input.length() > 0) {
                input = input.substring(0, input.length() - 1);
                if (input.isEmpty() || "-".equals(input)) {
                    input = "0";
                    isNewInput = true;
                }
                tvInput.setText(input);
            }

        } else if (id == R.id.button_plus_minus) {
            if (!"0".equals(input)) {
                if (input.startsWith("-")) {
                    input = input.substring(1);
                } else {
                    input = "-" + input;
                }
                tvInput.setText(input);
            }

        } else if (id == R.id.button_dot) {
            if (isNewInput) {
                input = "0.";
                isNewInput = false;
            } else if (!input.contains(".")) {
                input += ".";
            }
            tvInput.setText(input);

        } else if (id == R.id.button_percent) {
            double value = Double.parseDouble(input) / 100.0;
            input = formatNumber(value);
            tvInput.setText(input);
            isNewInput = true;
        }
    }
}