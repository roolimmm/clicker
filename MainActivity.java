package com.example.clicker;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.net.URL;
import java.net.HttpURLConnection;

public class MainActivity extends AppCompatActivity {
    private static final String BASE_URL = "http://10.0.2.2:9999/clicker/select?choice=";
    private String studentId;
    private String studentName;
    private TextView tvWelcome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get student info from intent
        studentId = getIntent().getStringExtra("STUDENT_ID");
        studentName = getIntent().getStringExtra("STUDENT_NAME");

        // Set up welcome message
        tvWelcome = findViewById(R.id.tvWelcome);
        if (studentName != null && !studentName.isEmpty()) {
            tvWelcome.setText("Welcome, " + studentName + "!");
            tvWelcome.setVisibility(View.VISIBLE);
        } else {
            tvWelcome.setVisibility(View.GONE);
        }

        Button buttonA = findViewById(R.id.buttonA);
        Button buttonB = findViewById(R.id.buttonB);
        Button buttonC = findViewById(R.id.buttonC);
        Button buttonD = findViewById(R.id.buttonD);

        buttonA.setOnClickListener(v -> sendResponse("a"));
        buttonB.setOnClickListener(v -> sendResponse("b"));
        buttonC.setOnClickListener(v -> sendResponse("c"));
        buttonD.setOnClickListener(v -> sendResponse("d"));
    }

    private void sendResponse(String choice) {
        new Thread(() -> {
            try {
                // Add student ID to the request if available
                String url = BASE_URL + choice;
                if (studentId != null && !studentId.isEmpty()) {
                    url += "&student_id=" + studentId;
                }

                URL requestUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Response successful
                    runOnUiThread(() -> Toast.makeText(MainActivity.this,
                            "Response " + choice + " recorded", Toast.LENGTH_SHORT).show());
                }
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}