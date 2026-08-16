package com.skillsync.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LandingActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);

        sessionManager = new SessionManager(this);

        setupHeroTitle();
        setupNavbar();
        setupClickListeners();
    }

    private void setupHeroTitle() {
        TextView heroTitleText = findViewById(R.id.heroTitleText);
        String fullText = "Discover your ideal Career Path";
        SpannableString spannable = new SpannableString(fullText);
        int start = fullText.indexOf("Career Path");
        int end = start + "Career Path".length();
        spannable.setSpan(new ForegroundColorSpan(0xFF185FA5), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        heroTitleText.setText(spannable);
    }

    private void setupNavbar() {
        TextView navLoginText = findViewById(R.id.navLoginText);
        String token = sessionManager.getToken();
        if (token != null && !token.isEmpty()) {
            navLoginText.setText("Dashboard");
            navLoginText.setOnClickListener(v -> startActivity(new Intent(this, DashboardActivity.class)));
        } else {
            navLoginText.setText("Log in");
            navLoginText.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.navStartAnalysisButton).setOnClickListener(v -> goToAnalysis());
        findViewById(R.id.heroStartAnalysisButton).setOnClickListener(v -> goToAnalysis());
        findViewById(R.id.ctaStartButton).setOnClickListener(v -> goToAnalysis());

        findViewById(R.id.heroHowItWorksButton).setOnClickListener(v -> scrollToHowItWorks());

        findViewById(R.id.footerCareerAnalysisLink).setOnClickListener(v -> goToAnalysis());
        findViewById(R.id.footerHowItWorksLink).setOnClickListener(v -> scrollToHowItWorks());
        findViewById(R.id.footerDashboardLink).setOnClickListener(v -> {
            String token = sessionManager.getToken();
            if (token != null && !token.isEmpty()) {
                startActivity(new Intent(this, DashboardActivity.class));
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
        });
        findViewById(R.id.footerLoginLink).setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        findViewById(R.id.footerSignupLink).setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
    }

    private void goToAnalysis() {
        startActivity(new Intent(this, AnalysisActivity.class));
    }

    private void scrollToHowItWorks() {
        ScrollView scrollView = findViewById(R.id.landingScrollView);
        View target = findViewById(R.id.howItWorksSection);
        scrollView.post(() -> scrollView.smoothScrollTo(0, target.getTop()));
    }
}