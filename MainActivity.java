package com.example.clicker;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.net.URL;
import java.net.HttpURLConnection;

// MainActivity.java
public class MainActivity extends AppCompatActivity {
    private static final String BASE_URL = "http://10.0.2.2:9999/clicker/select?choice=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                URL url = new URL(BASE_URL + choice);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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