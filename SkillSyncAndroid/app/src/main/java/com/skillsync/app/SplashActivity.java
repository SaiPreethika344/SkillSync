package com.skillsync.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        SessionManager sessionManager = new SessionManager(this);
        setupButtons(sessionManager);
        populateFeatures();
        populateHowItWorks();
        populateFooterTags();
    }

    private void setupButtons(SessionManager sessionManager) {
        TextView loginLink = findViewById(R.id.loginLink);
        MaterialButton startAnalysisButton = findViewById(R.id.startAnalysisButton);
        MaterialButton heroPrimaryButton = findViewById(R.id.heroPrimaryButton);
        MaterialButton heroSecondaryButton = findViewById(R.id.heroSecondaryButton);
        MaterialButton ctaButton = findViewById(R.id.ctaButton);
        View howItWorksSection = findViewById(R.id.howItWorksSection);

        View.OnClickListener analysisClick = v -> {
            Intent target = sessionManager.isLoggedIn()
                    ? new Intent(this, AnalysisActivity.class)
                    : new Intent(this, LoginActivity.class);
            startActivity(target);
        };

        if (sessionManager.isLoggedIn()) {
            loginLink.setText("Dashboard");
            loginLink.setOnClickListener(v ->
                    startActivity(new Intent(this, DashboardActivity.class)));
        } else {
            loginLink.setText("Log in");
            loginLink.setOnClickListener(v ->
                    startActivity(new Intent(this, LoginActivity.class)));
        }

        startAnalysisButton.setOnClickListener(analysisClick);
        heroPrimaryButton.setOnClickListener(analysisClick);
        ctaButton.setOnClickListener(analysisClick);
        heroSecondaryButton.setOnClickListener(v ->
                howItWorksSection.requestFocus());
    }

    private void populateFeatures() {
        LinearLayout container = findViewById(R.id.featuresContainer);
        container.removeAllViews();

        container.addView(createFeatureCard("Resume intelligence",
                "Upload your resume and our AI extracts every skill, experience and strength automatically.",
                "\uD83D\uDCC4"));
        container.addView(createFeatureCard("Skill gap analysis",
                "See exactly which skills you have, which you are missing, and how close you are to your target role.",
                "\uD83D\uDCCA"));
        container.addView(createFeatureCard("Live job matches",
                "Get matched to real job listings with salary data pulled live from the job market.",
                "\uD83D\uDCBC"));
    }

    private void populateHowItWorks() {
        LinearLayout container = findViewById(R.id.howItWorksContainer);
        container.removeAllViews();
        container.addView(createHowItWorksCard("01", "Upload",
                "Upload your resume PDF or manually select your skills from our library."));
        container.addView(createHowItWorksCard("02", "Analyze",
                "Our AI processes your profile and maps it against thousands of career paths."));
        container.addView(createHowItWorksCard("03", "Discover",
                "Get your personalized career match report with a learning roadmap."));
    }

    private void populateFooterTags() {
        LinearLayout container = findViewById(R.id.footerTagContainer);
        container.removeAllViews();
        container.addView(createFooterTag("Engineering"));
        container.addView(createFooterTag("Medical"));
        container.addView(createFooterTag("Commerce"));
        container.addView(createFooterTag("Arts"));
    }

    private View createFeatureCard(String title, String description, String icon) {
        CardView card = baseCard(20, 24);
        LinearLayout layout = verticalLayout();

        TextView iconView = new TextView(this);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        iconView.setLayoutParams(iconParams);
        iconView.setBackgroundColor(Color.parseColor("#E6F1FB"));
        iconView.setGravity(android.view.Gravity.CENTER);
        iconView.setText(icon);
        iconView.setTextSize(22f);

        TextView titleView = new TextView(this);
        LinearLayout.LayoutParams titleParams = wrapParams();
        titleParams.topMargin = dp(20);
        titleView.setLayoutParams(titleParams);
        titleView.setText(title);
        titleView.setTextColor(Color.parseColor("#111111"));
        titleView.setTextSize(18f);
        titleView.setTypeface(titleView.getTypeface(), android.graphics.Typeface.BOLD);

        TextView bodyView = new TextView(this);
        LinearLayout.LayoutParams bodyParams = wrapParams();
        bodyParams.topMargin = dp(8);
        bodyView.setLayoutParams(bodyParams);
        bodyView.setText(description);
        bodyView.setTextColor(Color.parseColor("#666666"));
        bodyView.setTextSize(14f);
        bodyView.setLineSpacing(0f, 1.6f);

        layout.addView(iconView);
        layout.addView(titleView);
        layout.addView(bodyView);
        card.addView(layout);
        return card;
    }

    private View createHowItWorksCard(String number, String title, String description) {
        LinearLayout card = new LinearLayout(this);
        LinearLayout.LayoutParams params = matchWrapParams();
        params.bottomMargin = dp(40);
        card.setLayoutParams(params);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(android.view.Gravity.CENTER_HORIZONTAL);

        TextView badge = new TextView(this);
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(dp(56), dp(56));
        badge.setLayoutParams(badgeParams);
        badge.setBackgroundColor(Color.parseColor("#185FA5"));
        badge.setGravity(android.view.Gravity.CENTER);
        badge.setText(number);
        badge.setTextColor(Color.WHITE);
        badge.setTextSize(18f);
        badge.setTypeface(badge.getTypeface(), android.graphics.Typeface.BOLD);

        TextView titleView = new TextView(this);
        LinearLayout.LayoutParams titleParams = wrapParams();
        titleParams.topMargin = dp(20);
        titleView.setLayoutParams(titleParams);
        titleView.setText(title);
        titleView.setTextColor(Color.parseColor("#111111"));
        titleView.setTextSize(22f);
        titleView.setTypeface(titleView.getTypeface(), android.graphics.Typeface.BOLD);

        TextView descView = new TextView(this);
        LinearLayout.LayoutParams descParams = matchWrapParams();
        descParams.topMargin = dp(8);
        descView.setLayoutParams(descParams);
        descView.setGravity(android.view.Gravity.CENTER);
        descView.setText(description);
        descView.setTextColor(Color.parseColor("#666666"));
        descView.setTextSize(14f);
        descView.setLineSpacing(0f, 1.6f);

        card.addView(badge);
        card.addView(titleView);
        card.addView(descView);
        return card;
    }

    private View createFooterTag(String label) {
        TextView tag = new TextView(this);
        LinearLayout.LayoutParams params = wrapParams();
        params.rightMargin = dp(12);
        tag.setLayoutParams(params);
        tag.setText(label);
        tag.setTextColor(Color.parseColor("#60A5FA"));
        tag.setTextSize(11f);
        tag.setPadding(dp(8), dp(3), dp(8), dp(3));
        tag.setBackgroundColor(Color.parseColor("#1B2A44"));
        return tag;
    }

    private CardView baseCard(int radiusDp, int bottomMarginDp) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams params = matchWrapParams();
        params.bottomMargin = dp(bottomMarginDp);
        card.setLayoutParams(params);
        card.setRadius(dp(radiusDp));
        card.setCardElevation(0f);
        card.setCardBackgroundColor(Color.WHITE);
        return card;
    }

    private LinearLayout verticalLayout() {
        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(matchWrapParams());
        layout.setOrientation(LinearLayout.VERTICAL);
        return layout;
    }

    private TextView smallGrayText(String text, float sizeSp) {
        TextView textView = new TextView(this);
        textView.setLayoutParams(wrapParams());
        textView.setText(text);
        textView.setTextColor(Color.parseColor("#999999"));
        textView.setTextSize(sizeSp);
        return textView;
    }

    private LinearLayout.LayoutParams matchWrapParams() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private LinearLayout.LayoutParams wrapParams() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}