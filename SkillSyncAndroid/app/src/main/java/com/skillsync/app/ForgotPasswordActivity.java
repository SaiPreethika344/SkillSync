package com.skillsync.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 3-step forgot-password flow mirroring ForgotPasswordPage.jsx:
 *   EMAIL  → POST /auth/forgot-password  → advance to OTP step
 *   OTP    → client-only verify          → advance to RESET step
 *             resend → POST /auth/forgot-password again
 *   RESET  → POST /auth/reset-password   → success banner → LoginActivity after 2 s
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private enum Step { EMAIL, OTP, RESET }

    // Step containers
    private LinearLayout stepEmail, stepOtp, stepReset;

    // Shared message cards
    private LinearLayout errorCard, successCard;
    private TextView errorText, successText;

    // Email step
    private com.google.android.material.textfield.TextInputEditText emailInput;
    private com.google.android.material.button.MaterialButton sendOtpButton;
    private ProgressBar progressBar;

    // OTP step
    private TextView otpSubtitle;
    private com.google.android.material.textfield.TextInputEditText otpInput;
    private com.google.android.material.button.MaterialButton verifyOtpButton;
    private TextView resendOtpLink;
    private ProgressBar progressBarOtp;

    // Reset step
    private com.google.android.material.textfield.TextInputEditText newPasswordInput, confirmPasswordInput;
    private com.google.android.material.button.MaterialButton resetPasswordButton;
    private ProgressBar progressBarReset;

    // Captured across steps
    private String capturedEmail = "";
    private String capturedOtp = "";

    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        apiClient = new ApiClient();

        // Step containers
        stepEmail = findViewById(R.id.stepEmail);
        stepOtp   = findViewById(R.id.stepOtp);
        stepReset = findViewById(R.id.stepReset);

        // Shared message cards
        errorCard   = findViewById(R.id.errorCard);
        successCard = findViewById(R.id.successCard);
        errorText   = findViewById(R.id.errorText);
        successText = findViewById(R.id.successText);

        // Email step views
        emailInput    = findViewById(R.id.emailInput);
        sendOtpButton = findViewById(R.id.sendOtpButton);
        progressBar   = findViewById(R.id.progressBar);

        // OTP step views
        otpSubtitle      = findViewById(R.id.otpSubtitle);
        otpInput         = findViewById(R.id.otpInput);
        verifyOtpButton  = findViewById(R.id.verifyOtpButton);
        resendOtpLink    = findViewById(R.id.resendOtpLink);
        progressBarOtp   = findViewById(R.id.progressBarOtp);

        // Reset step views
        newPasswordInput     = findViewById(R.id.newPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        resetPasswordButton  = findViewById(R.id.resetPasswordButton);
        progressBarReset     = findViewById(R.id.progressBarReset);

        // Back to login
        TextView backToLoginLink = findViewById(R.id.backToLoginLink);

        // Wire clicks
        sendOtpButton.setOnClickListener(v -> sendOtp());
        verifyOtpButton.setOnClickListener(v -> verifyOtp());
        resendOtpLink.setOnClickListener(v -> resendOtp());
        resetPasswordButton.setOnClickListener(v -> resetPassword());
        backToLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step navigation
    // ─────────────────────────────────────────────────────────────────────────

    private void showStep(Step step) {
        clearMessages();
        stepEmail.setVisibility(step == Step.EMAIL ? View.VISIBLE : View.GONE);
        stepOtp.setVisibility(step  == Step.OTP   ? View.VISIBLE : View.GONE);
        stepReset.setVisibility(step == Step.RESET ? View.VISIBLE : View.GONE);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step: EMAIL — POST /auth/forgot-password {email}
    // ─────────────────────────────────────────────────────────────────────────

    private void sendOtp() {
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";
        if (email.isEmpty()) {
            showError("Please enter your email address.");
            return;
        }
        capturedEmail = email;
        clearMessages();
        setLoadingEmail(true);

        apiClient.forgotPassword(email, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    setLoadingEmail(false);
                    showError("Unable to send OTP. Please try again.");
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                runOnUiThread(() -> {
                    setLoadingEmail(false);
                    if (response.isSuccessful()) {
                        otpSubtitle.setText("We sent a 6-digit OTP to " + capturedEmail);
                        showStep(Step.OTP);
                    } else if (response.code() == 404) {
                        showError("No account found with this email");
                    } else {
                        showError(extractMessage(body, "Unable to send OTP. Please try again."));
                    }
                });
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step: OTP — verify (client-only) and resend
    // ─────────────────────────────────────────────────────────────────────────

    private void verifyOtp() {
        // Client-only: just capture value and advance — no API call
        String otp = otpInput.getText() != null ? otpInput.getText().toString().trim() : "";
        if (otp.isEmpty()) {
            showError("Please enter the OTP.");
            return;
        }
        capturedOtp = otp;
        showStep(Step.RESET);
    }

    private void resendOtp() {
        clearMessages();
        setLoadingOtp(true);

        apiClient.forgotPassword(capturedEmail, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    setLoadingOtp(false);
                    showError("Unable to resend OTP. Please try again.");
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                runOnUiThread(() -> {
                    setLoadingOtp(false);
                    if (response.isSuccessful()) {
                        showSuccess("OTP resent. Check your email.");
                    } else if (response.code() == 404) {
                        showError("No account found with this email");
                    } else {
                        showError(extractMessage(body, "Unable to resend OTP. Please try again."));
                    }
                });
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Step: RESET — POST /auth/reset-password {email, otp, newPassword}
    // ─────────────────────────────────────────────────────────────────────────

    private void resetPassword() {
        String newPassword     = newPasswordInput.getText()     != null ? newPasswordInput.getText().toString()     : "";
        String confirmPassword = confirmPasswordInput.getText() != null ? confirmPasswordInput.getText().toString() : "";

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            showError("Passwords do not match");
            return;
        }

        clearMessages();
        setLoadingReset(true);

        apiClient.resetPassword(capturedEmail, capturedOtp, newPassword, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    setLoadingReset(false);
                    showError("Unable to reset password. Please try again.");
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                runOnUiThread(() -> {
                    setLoadingReset(false);
                    if (response.isSuccessful()) {
                        // Match JSX: show success banner, then navigate to Login after 2000 ms
                        showSuccess("Password reset! Redirecting to login...");
                        resetPasswordButton.setEnabled(false);
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }, 2000);
                    } else {
                        showError(extractMessage(body, "Unable to reset password. Please try again."));
                    }
                });
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void showError(String message) {
        errorCard.setVisibility(View.VISIBLE);
        errorText.setText(message.startsWith("⚠️") ? message : "⚠️ " + message);
        successCard.setVisibility(View.GONE);
    }

    private void showSuccess(String message) {
        successCard.setVisibility(View.VISIBLE);
        successText.setText(message.startsWith("✓") ? message : "✓ " + message);
        errorCard.setVisibility(View.GONE);
    }

    private void clearMessages() {
        errorCard.setVisibility(View.GONE);
        successCard.setVisibility(View.GONE);
    }

    private void setLoadingEmail(boolean loading) {
        sendOtpButton.setEnabled(!loading);
        sendOtpButton.setText(loading ? "Sending OTP..." : "Send OTP");
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void setLoadingOtp(boolean loading) {
        resendOtpLink.setEnabled(!loading);
        progressBarOtp.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void setLoadingReset(boolean loading) {
        resetPasswordButton.setEnabled(!loading);
        resetPasswordButton.setText(loading ? "Resetting password..." : "Reset Password");
        progressBarReset.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private String extractMessage(String body, String fallback) {
        try {
            JSONObject json = new JSONObject(body);
            String msg = json.optString("message");
            return (msg != null && !msg.isEmpty()) ? msg : fallback;
        } catch (Exception e) {
            return fallback;
        }
    }
}
