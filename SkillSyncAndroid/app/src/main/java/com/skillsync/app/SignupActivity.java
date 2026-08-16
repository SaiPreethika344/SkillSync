package com.skillsync.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class SignupActivity extends AppCompatActivity {
    private com.google.android.material.textfield.TextInputEditText nameInput;
    private com.google.android.material.textfield.TextInputEditText emailInput;
    private com.google.android.material.textfield.TextInputEditText passwordInput;
    private com.google.android.material.button.MaterialButton signupButton;
    private ProgressBar progressBar;
    private LinearLayout errorCard;
    private TextView errorText;
    private ApiClient apiClient;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        apiClient = new ApiClient();
        sessionManager = new SessionManager(this);

        nameInput    = findViewById(R.id.nameInput);
        emailInput   = findViewById(R.id.emailInput);
        passwordInput= findViewById(R.id.passwordInput);
        signupButton = findViewById(R.id.signupButton);
        progressBar  = findViewById(R.id.progressBar);
        errorCard    = findViewById(R.id.errorCard);
        errorText    = findViewById(R.id.errorText);
        TextView loginLink = findViewById(R.id.loginLink);

        signupButton.setOnClickListener(v -> attemptSignup());
        loginLink.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void attemptSignup() {
        String name     = nameInput.getText() != null     ? nameInput.getText().toString().trim()     : "";
        String email    = emailInput.getText() != null    ? emailInput.getText().toString().trim()    : "";
        String password = passwordInput.getText() != null ? passwordInput.getText().toString().trim() : "";
        showError(null);

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        setLoading(true);
        apiClient.signup(name, email, password, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showError("Cannot connect to server. Please try again later.");
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                runOnUiThread(() -> {
                    setLoading(false);
                    if (response.isSuccessful()) {
                        try {
                            JSONObject json = new JSONObject(body);
                            String token = json.optString("token");
                            if (token.isEmpty()) token = json.optString("accessToken");
                            if (!token.isEmpty()) sessionManager.saveToken(token);
                            String displayName = name.isEmpty()
                                    ? (email.contains("@") ? email.substring(0, email.indexOf('@')) : "SkillSync User")
                                    : name;
                            sessionManager.saveUser(displayName, email);
                            startActivity(new Intent(SignupActivity.this, DashboardActivity.class));
                            finish();
                        } catch (Exception e) {
                            showError("Registration failed. Please try again.");
                        }
                    } else {
                        String msg = extractMessage(body);
                        showError(msg != null ? msg : "Registration failed. Please try again.");
                    }
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        signupButton.setEnabled(!loading);
        signupButton.setText(loading ? getString(R.string.creating_account) : getString(R.string.create_account));
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        if (message == null || message.isEmpty()) {
            errorCard.setVisibility(View.GONE);
            errorText.setText("");
            return;
        }
        errorCard.setVisibility(View.VISIBLE);
        errorText.setText(message.startsWith("⚠️") ? message : "⚠️ " + message);
    }

    private String extractMessage(String body) {
        try {
            if (body == null || body.isEmpty()) return null;
            JSONObject json = new JSONObject(body);
            String msg = json.optString("message");
            return (msg != null && !msg.isEmpty()) ? msg : null;
        } catch (Exception e) {
            return body.isEmpty() ? null : body;
        }
    }
}
