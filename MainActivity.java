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
    private TextView tvQuestion;
    private int currentQuestionNumber = 1;
    private static final int TOTAL_QUESTIONS = 5; // Based on your database
    private Button buttonA, buttonB, buttonC, buttonD, buttonNext, buttonExit;
    private boolean hasAnswered = false;

    // Sample questions - in a real app, you would fetch these from your servlet
    private String[] questions = {
            "What is the capital of France?",
            "Which planet is known as the Red Planet?",
            "What is the largest ocean on Earth?",
            "Which element has the chemical symbol \"O\"?",
            "Who wrote \"Romeo and Juliet\"?"
    };

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

        // Initialize question text view
        tvQuestion = findViewById(R.id.tvQuestion);

        // Initialize buttons
        buttonA = findViewById(R.id.buttonA);
        buttonB = findViewById(R.id.buttonB);
        buttonC = findViewById(R.id.buttonC);
        buttonD = findViewById(R.id.buttonD);
        buttonNext = findViewById(R.id.buttonNext);
        buttonExit = findViewById(R.id.buttonExit);
        buttonExit.setOnClickListener(v -> finishAffinity()); // closes all activities


        // Set button click listeners
        buttonA.setOnClickListener(v -> {
            if (!hasAnswered) {
                sendResponse("optionA");
            } else {
                showAlreadyAnsweredMessage();
            }
        });

        buttonB.setOnClickListener(v -> {
            if (!hasAnswered) {
                sendResponse("optionB");
            } else {
                showAlreadyAnsweredMessage();
            }
        });

        buttonC.setOnClickListener(v -> {
            if (!hasAnswered) {
                sendResponse("optionC");
            } else {
                showAlreadyAnsweredMessage();
            }
        });

        buttonD.setOnClickListener(v -> {
            if (!hasAnswered) {
                sendResponse("optionD");
            } else {
                showAlreadyAnsweredMessage();
            }
        });

        // Next button click listener
        buttonNext.setOnClickListener(v -> showNextQuestion());

        // Initially hide the next button until an answer is selected
        buttonNext.setVisibility(View.GONE);

        // Show the first question
        showQuestion(currentQuestionNumber);
    }

    private void showQuestion(int questionNumber) {
        // Reset answer state for new question
        hasAnswered = false;

        // Enable all answer buttons
        setAnswerButtonsEnabled(true);

        // Reset button appearance
        resetButtonAppearance();

        // Hide next button
        buttonNext.setVisibility(View.GONE);

        // Check if we have more questions
        if (questionNumber <= TOTAL_QUESTIONS) {
            // Display the current question
            tvQuestion.setText("Question " + questionNumber + ": " + questions[questionNumber - 1]);
        } else {
            // No more questions
            tvQuestion.setText("You have completed all questions!");
            setAnswerButtonsEnabled(false);
            buttonNext.setVisibility(View.GONE);
            buttonExit.setVisibility(View.VISIBLE); // Show Exit
        }
    }

    private void showNextQuestion() {
        currentQuestionNumber++;
        if (currentQuestionNumber <= TOTAL_QUESTIONS) {
            showQuestion(currentQuestionNumber);
        } else {
            tvQuestion.setText("You have completed all questions!");
            setAnswerButtonsEnabled(false);
            buttonNext.setVisibility(View.GONE);
            buttonExit.setVisibility(View.VISIBLE); // Show Exit
        }
    }

    private void setAnswerButtonsEnabled(boolean enabled) {
        buttonA.setEnabled(enabled);
        buttonB.setEnabled(enabled);
        buttonC.setEnabled(enabled);
        buttonD.setEnabled(enabled);
    }

    private void resetButtonAppearance() {
        buttonA.setAlpha(1.0f);
        buttonB.setAlpha(1.0f);
        buttonC.setAlpha(1.0f);
        buttonD.setAlpha(1.0f);
    }

    private void showAlreadyAnsweredMessage() {
        Toast.makeText(this, "You've already answered this question", Toast.LENGTH_SHORT).show();
    }

    private void sendResponse(String choice) {
        // Convert "optionA" to "a", "optionB" to "b", etc.
        String choiceLetter = choice.replace("option", "").toLowerCase();

        new Thread(() -> {
            try {
                // Build the URL with the choice parameter
                String url = BASE_URL + choiceLetter;  // Use the single letter

                // Add question number to URL
                url += "&questionNo=" + currentQuestionNumber;

                // Add student ID if available
                if (studentId != null && !studentId.isEmpty()) {
                    url += "&student_id=" + studentId;
                }

                URL requestUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Mark that we've answered this question
                    hasAnswered = true;

                    // Update UI on the main thread
                    runOnUiThread(() -> {
                        // Show success message
                        Toast.makeText(MainActivity.this,
                                "Response " + choice.replace("option", "") + " recorded for Question " + currentQuestionNumber,
                                Toast.LENGTH_SHORT).show();

                        // Highlight the selected answer
                        highlightSelectedButton(choice);

                        // Show the next button
                        buttonNext.setVisibility(View.VISIBLE);
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this,
                                "Error: Server returned code " + responseCode,
                                Toast.LENGTH_SHORT).show();
                    });
                }
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(MainActivity.this,
                        "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void highlightSelectedButton(String choice) {
        // Dim all buttons
        buttonA.setAlpha(0.5f);
        buttonB.setAlpha(0.5f);
        buttonC.setAlpha(0.5f);
        buttonD.setAlpha(0.5f);

        // Highlight the selected button
        switch (choice) {
            case "optionA":
                buttonA.setAlpha(1.0f);
                break;
            case "optionB":
                buttonB.setAlpha(1.0f);
                break;
            case "optionC":
                buttonC.setAlpha(1.0f);
                break;
            case "optionD":
                buttonD.setAlpha(1.0f);
                break;
        }
    }

}