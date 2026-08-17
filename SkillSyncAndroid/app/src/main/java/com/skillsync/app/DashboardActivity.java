package com.skillsync.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DashboardActivity extends AppCompatActivity {
    private TextView welcomeText;
    private TextView dashboardText;
    private TextView roadmapSummaryText;
    private LinearLayout metricsRow;
    private LinearLayout careerMatchesContainer;
    private LinearLayout skillStrengthContainer;
    private LinearLayout roadmapContainer;
    private LinearLayout chatMessagesContainer;
    private MaterialCardView chatPanel;
    private TextView chatTooltip;
    private EditText chatInput;
    private SessionManager sessionManager;
    private ApiClient apiClient;

    // Career selection state — mirrors DashboardPage.jsx selectedCareer useState
    private JSONArray cachedCareers = null;   // full careers array from /dashboard
    private int selectedCareerIndex = 0;       // which card is currently selected
    private TextView metricMatchScoreValue;    // ref to the "Career match score" value TextView
    private TextView metricMatchScoreSubtitle; // ref to the subtitle under the match score

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        sessionManager = new SessionManager(this);
        apiClient = new ApiClient();

        welcomeText = findViewById(R.id.welcomeText);
        dashboardText = findViewById(R.id.dashboardText);
        roadmapSummaryText = findViewById(R.id.roadmapSummaryText);
        metricsRow = findViewById(R.id.metricsRow);
        careerMatchesContainer = findViewById(R.id.careerMatchesContainer);
        skillStrengthContainer = findViewById(R.id.skillStrengthContainer);
        roadmapContainer = findViewById(R.id.roadmapContainer);
        chatMessagesContainer = findViewById(R.id.chatMessagesContainer);
        chatPanel = findViewById(R.id.chatPanel);
        chatTooltip = findViewById(R.id.chatTooltip);
        chatInput = findViewById(R.id.chatInput);
        findViewById(R.id.newAnalysisButton).setOnClickListener(v ->
                startActivity(new Intent(this, AnalysisActivity.class)));
        findViewById(R.id.logoutLink).setOnClickListener(v -> {
            sessionManager.clearSession();
            Intent intent = new Intent(this, LandingActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
        FloatingActionButton chatFab = findViewById(R.id.chatFab);
        chatFab.setOnClickListener(v -> toggleChatPanel());
        findViewById(R.id.chatClose).setOnClickListener(v -> toggleChatPanel());
        findViewById(R.id.chatSendButton).setOnClickListener(v -> sendChatMessage());

        addBotMessage("Hi " + sessionManager.getUserName() + "! I'm your SkillSync AI career guide. Ask me anything about your roadmap or career path!");

        welcomeText.setText(getGreeting() + ", " + sessionManager.getUserName());
        loadDashboard();
    }

    private void loadDashboard() {
        apiClient.getDashboard(sessionManager.getToken(), new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    dashboardText.setText(getString(R.string.dashboard_placeholder));
                    Toast.makeText(DashboardActivity.this, getString(R.string.network_error), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject json = new JSONObject(body);
                            String userName = json.optString("userName");
                            if (!userName.isEmpty()) {
                                sessionManager.saveUser(userName, sessionManager.getUserEmail());
                                welcomeText.setText(getGreeting() + ", " + userName);
                            }

                            String dashboardSummary = formatDashboardSummary(json);
                            if (dashboardSummary.isEmpty()) {
                                dashboardSummary = getString(R.string.dashboard_placeholder);
                            }
                            dashboardText.setText(dashboardSummary);
                            renderDashboard(json);
                        } catch (Exception e) {
                            dashboardText.setText(getString(R.string.dashboard_placeholder));
                        }
                    } else {
                        dashboardText.setText(getString(R.string.dashboard_placeholder));
                    }
                });
            }
        });
    }

    private void renderDashboard(JSONObject json) {
        metricsRow.removeAllViews();
        careerMatchesContainer.removeAllViews();
        skillStrengthContainer.removeAllViews();
        roadmapContainer.removeAllViews();

        JSONArray careers = json.optJSONArray("topCareerMatches");
        JSONArray skillStrengths = json.optJSONArray("skillStrengths");

        // isMatchScore=true so we keep a ref for live updates when career card is clicked
        // mirrors DashboardPage.jsx line 377: selectedCareer?.matchPercentage ?? topCareerMatchScore
        addMetricCard("Career match score",
                String.valueOf(json.optInt("topCareerMatchScore", 0)) + "%",
                careers != null && careers.length() > 0
                        ? careers.optJSONObject(0).optString("careerTitle", "Run an analysis")
                        : "Run an analysis",
                /*isMatchScore=*/true);
        addMetricCard("Skills identified",
                String.valueOf(skillStrengths != null ? skillStrengths.length() : 0),
                "From your analysis",
                /*isMatchScore=*/false);
        addMetricCard("Roadmap steps",
                String.valueOf(buildRoadmapSkills(careers).size()),
                "Suggested focus areas",
                /*isMatchScore=*/false);

        if (careers == null || careers.length() == 0) {
            addEmptyText(careerMatchesContainer, "No analysis yet. Start a new analysis to see career paths.");
        } else {
            // Cache careers for click-driven re-renders; default selected = index 0
            cachedCareers = careers;
            selectedCareerIndex = 0;
            renderCareerCards();
        }

        if (skillStrengths == null || skillStrengths.length() == 0) {
            addEmptyText(skillStrengthContainer, "Run an analysis to see your skill strengths.");
        } else {
            for (int i = 0; i < skillStrengths.length(); i++) {
                JSONObject skill = skillStrengths.optJSONObject(i);
                if (skill == null) {
                    continue;
                }
                skillStrengthContainer.addView(createSkillRow(skill));
            }
        }

        Set<String> roadmapSkills = buildRoadmapSkills(careers);
        roadmapSummaryText.setText(roadmapSkills.isEmpty()
                ? "Your personalized roadmap will appear after your first analysis."
                : roadmapSkills.size() + " priority skills to develop next");
        if (roadmapSkills.isEmpty()) {
            addEmptyText(roadmapContainer, "No roadmap available yet.");
        } else {
            int index = 1;
            for (String skill : roadmapSkills) {
                roadmapContainer.addView(createRoadmapCard(index, skill));
                index++;
            }
        }
    }

    private void addMetricCard(String label, String value, String subtitle, boolean isMatchScore) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(12);
        card.setLayoutParams(params);
        card.setRadius(dp(14));
        card.setCardBackgroundColor(Color.WHITE);
        card.setCardElevation(0f);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(20), dp(20), dp(20), dp(20));

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextColor(Color.parseColor("#999999"));
        labelView.setTextSize(12f);

        TextView valueView = new TextView(this);
        valueView.setText(value);
        valueView.setTextColor(Color.parseColor("#111111"));
        valueView.setTextSize(30f);
        valueView.setTypeface(valueView.getTypeface(), android.graphics.Typeface.BOLD);

        TextView subtitleView = new TextView(this);
        subtitleView.setText(subtitle);
        subtitleView.setTextColor(Color.parseColor("#999999"));
        subtitleView.setTextSize(12f);

        content.addView(labelView);
        content.addView(valueView);
        content.addView(subtitleView);
        card.addView(content);
        metricsRow.addView(card);

        // Keep refs so career switching can update the match score live
        // mirrors: DashboardPage.jsx line 377: selectedCareer?.matchPercentage
        if (isMatchScore) {
            metricMatchScoreValue    = valueView;
            metricMatchScoreSubtitle = subtitleView;
        }
    }

    /**
     * Re-renders all career match cards with the correct selected highlight.
     * Mirrors DashboardPage.jsx careers.map onClick={() => setSelectedCareer(c)).
     * Source: DashboardPage.jsx lines 403-418.
     */
    private void renderCareerCards() {
        careerMatchesContainer.removeAllViews();
        if (cachedCareers == null) return;
        int limit = Math.min(cachedCareers.length(), 5);
        for (int i = 0; i < limit; i++) {
            final int idx = i;
            JSONObject career = cachedCareers.optJSONObject(i);
            if (career == null) continue;
            boolean isSelected = (i == selectedCareerIndex);
            View card = createCareerMatchCard(career, isSelected);
            card.setOnClickListener(v -> {
                if (selectedCareerIndex == idx) return; // already selected
                selectedCareerIndex = idx;
                // Re-render all cards with new highlight (mirrors setSelectedCareer(c))
                renderCareerCards();
                // Update "Career match score" metric card
                // mirrors DashboardPage.jsx line 377: selectedCareer?.matchPercentage
                updateSelectedCareerMetric();
                // Fetch dynamic roadmap for non-top-1 career via /chat
                // mirrors DashboardPage.jsx useEffect on selectedCareer (lines 163-200)
                if (idx != 0) {
                    fetchDynamicRoadmap(career.optString("careerTitle", "your target career"));
                } else {
                    // Switching back to top career: restore the static missingSkills roadmap
                    if (cachedCareers != null) {
                        Set<String> skills = buildRoadmapSkills(cachedCareers);
                        roadmapContainer.removeAllViews();
                        roadmapSummaryText.setText(skills.isEmpty()
                                ? "Your personalized roadmap will appear after your first analysis."
                                : skills.size() + " priority skills to develop next");
                        if (skills.isEmpty()) {
                            addEmptyText(roadmapContainer, "No roadmap available yet.");
                        } else {
                            int index = 1;
                            for (String skill : skills) {
                                roadmapContainer.addView(createRoadmapCard(index, skill));
                                index++;
                            }
                        }
                    }
                }
            });
            careerMatchesContainer.addView(card);
        }
    }

    /**
     * Updates the "Career match score" metric card to reflect the currently selected career.
     * Mirrors DashboardPage.jsx line 377: selectedCareer?.matchPercentage ?? topCareerMatchScore
     */
    private void updateSelectedCareerMetric() {
        if (cachedCareers == null || metricMatchScoreValue == null) return;
        JSONObject career = cachedCareers.optJSONObject(selectedCareerIndex);
        if (career == null) return;
        metricMatchScoreValue.setText(career.optInt("matchPercentage", 0) + "%");
        if (metricMatchScoreSubtitle != null) {
            metricMatchScoreSubtitle.setText(career.optString("careerTitle", "Run an analysis"));
        }
    }

    /**
     * Calls /chat to generate a 6-step learning roadmap for the given career.
     * Mirrors DashboardPage.jsx useEffect on selectedCareer (lines 163-200):
     *
     *   body: {
     *     message: `Generate a learning roadmap for someone who wants to become a ${careerTitle}.
     *               Give exactly 6 specific actionable steps numbered 1-6. Format each step as
     *               just the step title, one per line, no extra text.`,
     *     context: `Career roadmap for ${careerTitle}`
     *   }
     *   response parsing: text.split('\n'), strip leading numbers, filter length > 5, slice(0,6)
     */
    private void fetchDynamicRoadmap(String careerTitle) {
        roadmapContainer.removeAllViews();
        roadmapSummaryText.setText("Generating roadmap for " + careerTitle + "...");
        addEmptyText(roadmapContainer, "Loading AI roadmap...");

        String token = sessionManager.getToken();
        String message = "Generate a learning roadmap for someone who wants to become a "
                + careerTitle
                + ". Give exactly 6 specific actionable steps numbered 1-6."
                + " Format each step as just the step title, one per line, no extra text.";
        String context = "Career roadmap for " + careerTitle;

        apiClient.postChat(token, message, context, new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull java.io.IOException e) {
                runOnUiThread(() -> {
                    roadmapContainer.removeAllViews();
                    roadmapSummaryText.setText("Could not generate roadmap. Check your connection.");
                    addEmptyText(roadmapContainer, "Roadmap generation failed.");
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response)
                    throws java.io.IOException {
                String body = response.body() != null ? response.body().string() : "";
                runOnUiThread(() -> {
                    roadmapContainer.removeAllViews();
                    try {
                        JSONObject json = new JSONObject(body);
                        // mirrors: data.reply || data.response || data.message
                        String text = json.optString("reply",
                                json.optString("response",
                                        json.optString("message", "")));
                        // mirrors: text.split('\n')
                        //   .map(s => s.replace(/^\d+[\.\)]\s*/, '').replace(/\*\*/g,'').trim())
                        //   .filter(s => s.length > 5).slice(0, 6)
                        String[] lines = text.split("\n");
                        int stepIndex = 1;
                        for (String line : lines) {
                            String step = line
                                    .replaceAll("^\\d+[.)]\\s*", "")
                                    .replace("**", "")
                                    .trim();
                            if (step.length() > 5 && stepIndex <= 6) {
                                roadmapContainer.addView(createRoadmapCard(stepIndex, step));
                                stepIndex++;
                            }
                        }
                        int count = stepIndex - 1;
                        roadmapSummaryText.setText(count > 0
                                ? count + " priority steps for " + careerTitle
                                : "No roadmap steps returned.");
                        if (count == 0) {
                            addEmptyText(roadmapContainer, "Could not parse roadmap steps.");
                        }
                    } catch (Exception e) {
                        roadmapSummaryText.setText("Roadmap generation failed.");
                        addEmptyText(roadmapContainer, "Unexpected response from server.");
                    }
                });
            }
        });
    }

    private void toggleChatPanel() {
        boolean opening = chatPanel.getVisibility() != View.VISIBLE;
        chatPanel.setVisibility(opening ? View.VISIBLE : View.GONE);
        chatTooltip.setVisibility(opening ? View.GONE : View.VISIBLE);
    }

    /**
     * Sends a chat message to /chat and displays the AI reply.
     * Mirrors ChatBot.jsx sendMessage() → fetch('/chat', {message, context}).
     * Source: frontend/src/components/ChatBot.jsx.
     *
     * NOTE: Requires GROQ_API_KEY to be set in the backend terminal session:
     *   $env:GROQ_API_KEY = "gsk_your_key_here"
     * This key is NOT persisted automatically — must be re-set each time the backend restarts.
     * Do NOT commit the key to .env or any tracked file.
     */
    private void sendChatMessage() {
        String message = chatInput.getText().toString().trim();
        if (message.isEmpty()) return;

        addChatBubble(message, true);
        chatInput.setText("");

        // Show a typing indicator while waiting
        addBotMessage("Thinking...");
        // We'll replace the last bubble with the real reply
        final int thinkingBubbleIndex = chatMessagesContainer.getChildCount() - 1;

        String token = sessionManager.getToken();
        // context mirrors ChatBot.jsx: `Career advisor for ${topCareer}`
        String topCareer = (cachedCareers != null && cachedCareers.length() > 0)
                ? cachedCareers.optJSONObject(0).optString("careerTitle", "your target career")
                : "your target career";
        String context = "Career advisor for " + topCareer;

        apiClient.postChat(token, message, context, new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull java.io.IOException e) {
                runOnUiThread(() -> replaceThinkingBubble(thinkingBubbleIndex,
                        "Could not reach the server. Check your connection and that GROQ_API_KEY is set in the backend terminal."));
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response)
                    throws java.io.IOException {
                String body = response.body() != null ? response.body().string() : "";
                runOnUiThread(() -> {
                    try {
                        JSONObject json = new JSONObject(body);
                        // mirrors ChatBot.jsx: data.reply || data.response || data.message
                        String reply = json.optString("reply",
                                json.optString("response",
                                        json.optString("message", "No response from AI.")));
                        replaceThinkingBubble(thinkingBubbleIndex, reply);
                    } catch (Exception e) {
                        replaceThinkingBubble(thinkingBubbleIndex,
                                "Error parsing server response.");
                    }
                });
            }
        });
    }

    /** Replaces the "Thinking..." placeholder bubble with the actual AI reply. */
    private void replaceThinkingBubble(int index, String reply) {
        if (index >= 0 && index < chatMessagesContainer.getChildCount()) {
            chatMessagesContainer.removeViewAt(index);
        }
        addBotMessage(reply);
    }

    private void addBotMessage(String message) {
        addChatBubble(message, false);
    }

    private void addChatBubble(String message, boolean user) {
        LinearLayout row = new LinearLayout(this);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        rowParams.bottomMargin = dp(10);
        row.setLayoutParams(rowParams);
        row.setGravity(user ? Gravity.END : Gravity.START);

        TextView bubble = new TextView(this);
        LinearLayout.LayoutParams bubbleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        bubble.setLayoutParams(bubbleParams);
        bubble.setMaxWidth(dp(240));
        bubble.setPadding(dp(14), dp(10), dp(14), dp(10));
        bubble.setText(message);
        bubble.setTextSize(13f);
        bubble.setLineSpacing(0f, 1.5f);
        if (user) {
            bubble.setBackgroundColor(Color.parseColor("#185FA5"));
            bubble.setTextColor(Color.WHITE);
        } else {
            bubble.setBackgroundColor(Color.parseColor("#F5F5F5"));
            bubble.setTextColor(Color.parseColor("#111111"));
        }

        row.addView(bubble);
        chatMessagesContainer.addView(row);
    }

    private View createCareerMatchCard(JSONObject career, boolean highlight) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(10);
        card.setLayoutParams(params);
        card.setRadius(dp(10));
        card.setCardBackgroundColor(highlight ? Color.parseColor("#F0F5FB") : Color.WHITE);
        card.setCardElevation(0f);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(16), dp(16), dp(16));

        LinearLayout topRow = new LinearLayout(this);
        topRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView title = new TextView(this);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        title.setText(career.optString("careerTitle", "Career Match"));
        title.setTextColor(Color.parseColor("#111111"));
        title.setTextSize(13f);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);

        TextView score = new TextView(this);
        score.setText(career.optInt("matchPercentage", 0) + "%");
        score.setTextColor(Color.parseColor("#185FA5"));
        score.setTextSize(12f);
        score.setTypeface(score.getTypeface(), android.graphics.Typeface.BOLD);

        TextView description = new TextView(this);
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        descParams.topMargin = dp(4);
        description.setLayoutParams(descParams);
        description.setText(career.optString("description", ""));
        description.setTextColor(Color.parseColor("#999999"));
        description.setTextSize(12f);

        topRow.addView(title);
        topRow.addView(score);
        content.addView(topRow);
        content.addView(description);
        card.addView(content);
        return card;
    }

    private View createSkillRow(JSONObject skill) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(14);
        row.setLayoutParams(params);

        LinearLayout labelRow = new LinearLayout(this);
        labelRow.setOrientation(LinearLayout.HORIZONTAL);

        TextView name = new TextView(this);
        name.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        name.setText(skill.optString("skillName", "Skill"));
        name.setTextColor(Color.parseColor("#666666"));
        name.setTextSize(12f);

        TextView value = new TextView(this);
        int percentage = skill.optInt("percentage", 0);
        value.setText(percentage + "%");
        value.setTextColor(Color.parseColor("#999999"));
        value.setTextSize(12f);

        ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        LinearLayout.LayoutParams progressParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(8)
        );
        progressParams.topMargin = dp(8);
        progress.setLayoutParams(progressParams);
        progress.setMax(100);
        progress.setProgress(percentage);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(Color.parseColor(
                percentage >= 70 ? "#185FA5" : percentage >= 50 ? "#EF9F27" : "#E24B4A"
        )));

        labelRow.addView(name);
        labelRow.addView(value);
        row.addView(labelRow);
        row.addView(progress);
        return row;
    }

    private View createRoadmapCard(int index, String skill) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = dp(10);
        card.setLayoutParams(params);
        card.setRadius(dp(10));
        card.setCardBackgroundColor(Color.WHITE);
        card.setCardElevation(0f);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(14), dp(14), dp(14), dp(14));

        TextView badge = new TextView(this);
        badge.setText(String.format("%02d", index));
        badge.setTextColor(Color.parseColor("#185FA5"));
        badge.setTextSize(13f);
        badge.setTypeface(badge.getTypeface(), android.graphics.Typeface.BOLD);
        badge.setBackgroundColor(Color.parseColor("#E6F1FB"));
        badge.setPadding(dp(10), dp(8), dp(10), dp(8));

        TextView title = new TextView(this);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
        );
        titleParams.leftMargin = dp(12);
        title.setLayoutParams(titleParams);
        title.setText(skill);
        title.setTextColor(Color.parseColor("#111111"));
        title.setTextSize(13f);
        title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);

        row.addView(badge);
        row.addView(title);
        card.addView(row);
        return card;
    }

    private void addEmptyText(LinearLayout container, String text) {
        TextView placeholder = new TextView(this);
        placeholder.setText(text);
        placeholder.setTextColor(Color.parseColor("#999999"));
        placeholder.setTextSize(13f);
        container.addView(placeholder);
    }

    private Set<String> buildRoadmapSkills(JSONArray careers) {
        Set<String> roadmapSkills = new LinkedHashSet<>();
        if (careers == null) {
            return roadmapSkills;
        }

        for (int i = 0; i < careers.length() && roadmapSkills.size() < 6; i++) {
            JSONObject career = careers.optJSONObject(i);
            if (career == null) {
                continue;
            }
            JSONArray missing = career.optJSONArray("missingSkills");
            if (missing == null) {
                continue;
            }
            for (int j = 0; j < missing.length() && roadmapSkills.size() < 6; j++) {
                String skill = missing.optString(j);
                if (!skill.isEmpty()) {
                    roadmapSkills.add(skill);
                }
            }
        }
        return roadmapSkills;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    /** Matches DashboardPage.jsx: morning (<12) / afternoon (12-17) / evening (>=18) */
    private String getGreeting() {
        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
        if (hour < 12) return "Good morning";
        if (hour < 18) return "Good afternoon";
        return "Good evening";
    }

    private String formatDashboardSummary(JSONObject json) {
        StringBuilder builder = new StringBuilder();
        int topScore = json.optInt("topCareerMatchScore", -1);
        if (topScore >= 0) {
            builder.append("Top career match score: ").append(topScore).append("%");
        }

        JSONArray skillStrengths = json.optJSONArray("skillStrengths");
        if (skillStrengths != null && skillStrengths.length() > 0) {
            if (builder.length() > 0) {
                builder.append("\n\n");
            }
            builder.append("Skill strengths:");
            for (int i = 0; i < skillStrengths.length(); i++) {
                JSONObject skill = skillStrengths.optJSONObject(i);
                if (skill == null) {
                    continue;
                }
                builder.append("\n- ")
                        .append(skill.optString("skillName", "Skill"))
                        .append(": ")
                        .append(skill.optInt("percentage", 0))
                        .append("%");
            }
        }

        JSONArray topCareerMatches = json.optJSONArray("topCareerMatches");
        if (topCareerMatches != null && topCareerMatches.length() > 0) {
            if (builder.length() > 0) {
                builder.append("\n\n");
            }
            builder.append("Top career matches:");
            for (int i = 0; i < topCareerMatches.length(); i++) {
                JSONObject match = topCareerMatches.optJSONObject(i);
                if (match == null) {
                    continue;
                }

                builder.append("\n- ")
                        .append(match.optString("careerTitle", "Career Match"))
                        .append(": ")
                        .append(match.optInt("matchPercentage", 0))
                        .append("%");

                String description = match.optString("description");
                if (!description.isEmpty()) {
                    builder.append(" - ").append(description);
                }
            }
        }

        return builder.toString().trim();
    }
}
