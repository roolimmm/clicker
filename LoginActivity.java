package com.example.clicker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText editStudentId;
    private EditText editName;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize UI elements
        editStudentId = findViewById(R.id.editStudentId);
        editName = findViewById(R.id.editName);
        btnLogin = findViewById(R.id.btnLogin);

        // Set up login button click listener
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String studentId = editStudentId.getText().toString().trim();
                String name = editName.getText().toString().trim();

                // Simple validation
                if (studentId.isEmpty() || name.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter both fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Successful login, navigate to MainActivity
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    // Pass student info to MainActivity if needed
                    intent.putExtra("STUDENT_ID", studentId);
                    intent.putExtra("STUDENT_NAME", name);
                    startActivity(intent);

                    // Optionally show a toast
                    Toast.makeText(LoginActivity.this, "Welcome, " + name + "!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}