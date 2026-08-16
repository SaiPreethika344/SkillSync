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

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private com.google.android.material.button.MaterialButton loginButton;
    private ProgressBar progressBar;
    private LinearLayout errorCard;
    private TextView errorText;
    private TextView forgotPasswordLink;
    private ApiClient apiClient;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        apiClient = new ApiClient();
        sessionManager = new SessionManager(this);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
        errorCard = findViewById(R.id.errorCard);
        errorText = findViewById(R.id.errorText);
        TextView signupLink = findViewById(R.id.signupLink);

        loginButton.setOnClickListener(v -> attemptLogin());

        signupLink.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));

        forgotPasswordLink = findViewById(R.id.forgotPasswordLink);
        forgotPasswordLink.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void attemptLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        showError(null);

        if (email.isEmpty() || password.isEmpty()) {
            showError(getString(R.string.fill_all_fields));
            return;
        }

        setLoading(true);

        apiClient.login(email, password, new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

                e.printStackTrace();

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

                            if (token.isEmpty())
                                token = json.optString("accessToken");

                            if (token.isEmpty())
                                token = json.optString("jwt");

                            if (!token.isEmpty()) {
                                sessionManager.saveToken(token);
                            }

                            String name = json.optString("name");
                            String userEmail = json.optString("email", email);

                            if (name.isEmpty() && json.optJSONObject("user") != null) {
                                name = json.optJSONObject("user").optString("name");
                                userEmail = json.optJSONObject("user").optString("email", email);
                            }

                            if (name.isEmpty()) {
                                int atIndex = email.indexOf('@');
                                name = atIndex > 0 ? email.substring(0, atIndex) : "SkillSync User";
                            }

                            sessionManager.saveUser(name, userEmail);

                            Toast.makeText(
                                    LoginActivity.this,
                                    getString(R.string.login_success),
                                    Toast.LENGTH_SHORT
                            ).show();

                            startActivity(new Intent(LoginActivity.this, AnalysisActivity.class));

                            finish();

                        } catch (Exception ex) {
                            showError(getString(R.string.login_failed));
                        }

                    } else {
                        showError(resolveLoginError(response.code(), body));
                    }
                });
            }
        });
    }

    private void setLoading(boolean loading) {

        loginButton.setEnabled(!loading);

        loginButton.setText(
                loading ?
                        getString(R.string.logging_in) :
                        getString(R.string.login));

        progressBar.setVisibility(
                loading ?
                        android.view.View.VISIBLE :
                        android.view.View.GONE);
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

    private String resolveLoginError(int statusCode, String body) {
        if (statusCode == 401) {
            return "Invalid email or password. Please try again.";
        }
        if (statusCode == 404) {
            return "Account not found. Please sign up first.";
        }
        return "Invalid email or password. Please try again.";
    }
}
