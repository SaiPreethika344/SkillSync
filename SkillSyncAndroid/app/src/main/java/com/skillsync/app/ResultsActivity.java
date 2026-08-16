package com.skillsync.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * ResultsActivity — matches ResultsPage.jsx exactly:
 *
 *   Not logged in  → show lockGateCard + blurPreviewContainer; summaryText = "Log in to see..."
 *   Logged in, 0 careers → show emptyResultsCard
 *   Logged in, N careers → populate resultsContainer cards + unlockCard
 *
 * careers come from:
 *   1) Intent extra "analysis_json" → careerMatches array (fresh analysis)
 *   2) GET /dashboard → topCareerMatches (returning user, no fresh analysis)
 */
public class ResultsActivity extends AppCompatActivity {

    private TextView summaryText;
    private View lockGateCard;
    private View blurPreviewContainer;
    private LinearLayout resultsContainer;
    private View emptyResultsCard;
    private View unlockCard;

    private SessionManager sessionManager;
    private ApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        sessionManager = new SessionManager(this);
        apiClient      = new ApiClient();

        summaryText          = findViewById(R.id.summaryText);
        lockGateCard         = findViewById(R.id.lockGateCard);
        blurPreviewContainer = findViewById(R.id.blurPreviewContainer);
        resultsContainer     = findViewById(R.id.resultsContainer);
        emptyResultsCard     = findViewById(R.id.emptyResultsCard);
        unlockCard           = findViewById(R.id.unlockCard);

        // Wire buttons
        View loginBtn  = findViewById(R.id.loginForResultsButton);
        View signupBtn = findViewById(R.id.signupButton);
        View startBtn  = findViewById(R.id.startAnalysisButton);
        View dashBtn   = findViewById(R.id.dashboardButton);

        if (loginBtn  != null) loginBtn.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        if (signupBtn != null) signupBtn.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
        if (startBtn  != null) startBtn.setOnClickListener(v -> { startActivity(new Intent(this, AnalysisActivity.class)); finish(); });
        if (dashBtn   != null) dashBtn.setOnClickListener(v -> { startActivity(new Intent(this, DashboardActivity.class)); finish(); });

        String token = sessionManager.getToken();

        // ─── NOT LOGGED IN ───────────────────────────────────────────────────
        if (token == null || token.isEmpty()) {
            summaryText.setText("Log in to see your personalized career matches.");
            showNotLoggedIn();
            return;
        }

        summaryText.setText("Here are your top career matches based on your skills.");

        // ─── Try fresh analysis JSON from Intent ─────────────────────────────
        String analysisJson = getIntent().getStringExtra("analysis_json");
        if (analysisJson != null && !analysisJson.isEmpty()) {
            try {
                JSONObject json = new JSONObject(analysisJson);
                JSONArray matches = json.optJSONArray("careerMatches");
                if (matches != null && matches.length() > 0) {
                    List<JSONObject> sorted = sortAndSlice(matches, 5);
                    showCareers(sorted);
                    return;
                }
            } catch (Exception ignored) {}
        }

        // ─── Fallback: load from dashboard endpoint ───────────────────────────
        apiClient.getDashboard(token, new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> showEmpty());
            }

            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                runOnUiThread(() -> {
                    if (response.code() == 401) {
                        sessionManager.clearSession();
                        showNotLoggedIn();
                        return;
                    }
                    if (!response.isSuccessful()) { showEmpty(); return; }
                    try {
                        JSONObject json = new JSONObject(body);
                        JSONArray matches = json.optJSONArray("topCareerMatches");
                        if (matches != null && matches.length() > 0) {
                            showCareers(sortAndSlice(matches, 5));
                        } else {
                            showEmpty();
                        }
                    } catch (Exception e) {
                        showEmpty();
                    }
                });
            }
        });
    }

    // ─── Visibility states ───────────────────────────────────────────────────

    private void showNotLoggedIn() {
        lockGateCard.setVisibility(View.VISIBLE);
        blurPreviewContainer.setVisibility(View.VISIBLE);
        resultsContainer.setVisibility(View.GONE);
        emptyResultsCard.setVisibility(View.GONE);
        unlockCard.setVisibility(View.GONE);
    }

    private void showEmpty() {
        lockGateCard.setVisibility(View.GONE);
        blurPreviewContainer.setVisibility(View.GONE);
        resultsContainer.setVisibility(View.GONE);
        emptyResultsCard.setVisibility(View.VISIBLE);
        unlockCard.setVisibility(View.GONE);
    }

    private void showCareers(List<JSONObject> careers) {
        lockGateCard.setVisibility(View.GONE);
        blurPreviewContainer.setVisibility(View.GONE);
        resultsContainer.setVisibility(View.VISIBLE);
        emptyResultsCard.setVisibility(View.GONE);
        unlockCard.setVisibility(View.VISIBLE);
        populateCareerCards(resultsContainer, careers);
    }

    // ─── Career card builder ─────────────────────────────────────────────────

    /**
     * Mirrors JSX career card:
     *   i==0 → 2dp #185FA5 border, shadow, blue badge
     *   i>0  → 1dp #eee border, gray badge
     *   missingSkills → orange chip tags (#FFF7ED bg, #D97706 text, #FDC57B border)
     */
    private void populateCareerCards(LinearLayout container, List<JSONObject> careers) {
        container.removeAllViews();
        for (int i = 0; i < careers.size(); i++) {
            JSONObject c = careers.get(i);
            boolean isTop = (i == 0);

            MaterialCardView card = new MaterialCardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.bottomMargin = dp(16);
            card.setLayoutParams(cardParams);
            card.setCardBackgroundColor(Color.WHITE);
            card.setRadius(dp(16));
            card.setCardElevation(isTop ? dp(4) : 0f);
            card.setStrokeColor(isTop ? Color.parseColor("#185FA5") : Color.parseColor("#EEEEEE"));
            card.setStrokeWidth(isTop ? dp(2) : dp(1));

            LinearLayout content = new LinearLayout(this);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setPadding(dp(20), dp(20), dp(20), dp(20));

            // Top row: title + badge
            LinearLayout topRow = new LinearLayout(this);
            topRow.setOrientation(LinearLayout.HORIZONTAL);
            topRow.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            TextView title = new TextView(this);
            title.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            title.setText(c.optString("careerTitle", "Career Match"));
            title.setTextColor(Color.parseColor("#111111"));
            title.setTextSize(16f);
            title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);

            TextView badge = new TextView(this);
            badge.setText(c.optInt("matchPercentage", 0) + "% match");
            badge.setTextSize(13f);
            badge.setTypeface(badge.getTypeface(), android.graphics.Typeface.BOLD);
            badge.setPadding(dp(12), dp(4), dp(12), dp(4));
            badge.setBackgroundResource(isTop
                    ? R.drawable.bg_match_badge_blue
                    : R.drawable.bg_match_badge_gray);
            badge.setTextColor(isTop
                    ? Color.parseColor("#185FA5")
                    : Color.parseColor("#666666"));

            topRow.addView(title);
            topRow.addView(badge);
            content.addView(topRow);

            // Description
            String desc = c.optString("description", "");
            JSONArray missing = c.optJSONArray("missingSkills");
            if (!desc.isEmpty()) {
                TextView descView = new TextView(this);
                LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                dp.topMargin = this.dp(8);
                descView.setLayoutParams(dp);
                descView.setText(desc);
                descView.setTextColor(Color.parseColor("#666666"));
                descView.setTextSize(14f);
                content.addView(descView);
            }

            // Missing skills tags (#FFF7ED bg, #D97706 text, #FDC57B border, 20dp radius, fontSize 12)
            if (missing != null && missing.length() > 0) {
                TextView skillsLabel = new TextView(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                lp.topMargin = dp(12);
                skillsLabel.setLayoutParams(lp);
                skillsLabel.setText("Skills to develop:");
                skillsLabel.setTextColor(Color.parseColor("#999999"));
                skillsLabel.setTextSize(12f);
                content.addView(skillsLabel);

                ChipGroup chipGroup = new ChipGroup(this);
                LinearLayout.LayoutParams cgParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                cgParams.topMargin = dp(6);
                chipGroup.setLayoutParams(cgParams);
                chipGroup.setChipSpacingHorizontal(dp(6));
                chipGroup.setChipSpacingVertical(dp(4));

                for (int s = 0; s < missing.length(); s++) {
                    String skill = missing.optString(s);
                    if (skill.isEmpty()) continue;
                    Chip chip = new Chip(this);
                    chip.setText(skill);
                    chip.setClickable(false);
                    chip.setCheckable(false);
                    chip.setChipBackgroundColor(
                            android.content.res.ColorStateList.valueOf(Color.parseColor("#FFF7ED")));
                    chip.setChipStrokeColor(
                            android.content.res.ColorStateList.valueOf(Color.parseColor("#FDC57B")));
                    chip.setChipStrokeWidth(dp(1));
                    chip.setTextColor(Color.parseColor("#D97706"));
                    chip.setTextSize(12f);
                    chip.setChipCornerRadius(dp(20));
                    chip.setEnsureMinTouchTargetSize(false);
                    chipGroup.addView(chip);
                }
                content.addView(chipGroup);
            }

            card.addView(content);
            container.addView(card);
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    /** Sort by matchPercentage desc, take top N — mirrors JSX sort+slice */
    private List<JSONObject> sortAndSlice(JSONArray array, int max) {
        List<JSONObject> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.optJSONObject(i);
            if (obj != null) list.add(obj);
        }
        list.sort((a, b) -> b.optInt("matchPercentage", 0) - a.optInt("matchPercentage", 0));
        return list.subList(0, Math.min(max, list.size()));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
