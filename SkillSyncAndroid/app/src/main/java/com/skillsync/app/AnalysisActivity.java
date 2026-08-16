package com.skillsync.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class AnalysisActivity extends AppCompatActivity {
    private static final String TAG = "SkillSync.Analysis";
    private static final LinkedHashMap<String, LinkedHashMap<String, List<String>>> SKILL_CATEGORIES = new LinkedHashMap<>();

    static {
        LinkedHashMap<String, List<String>> engineering = new LinkedHashMap<>();
        engineering.put("Programming Languages", Arrays.asList("Python", "Java", "C", "C++", "JavaScript", "Kotlin"));
        engineering.put("Web Development", Arrays.asList("React", "Node.js", "HTML/CSS", "REST APIs"));
        engineering.put("Data & AI", Arrays.asList("Machine Learning", "SQL", "Power BI", "NLP"));
        engineering.put("Core Engineering", Arrays.asList("AutoCAD", "Embedded Systems", "IoT", "Robotics"));
        SKILL_CATEGORIES.put("Engineering & Technology", engineering);

        LinkedHashMap<String, List<String>> medical = new LinkedHashMap<>();
        medical.put("Clinical Skills", Arrays.asList("Patient Assessment", "ECG Reading", "Clinical Diagnosis", "First Aid"));
        medical.put("Research & Lab", Arrays.asList("Lab Techniques", "Research Methodology", "Medical Writing", "Pathology"));
        medical.put("Healthcare Tech", Arrays.asList("EHR", "Medical Imaging", "Telemedicine", "Hospital Management"));
        SKILL_CATEGORIES.put("Medical & Health Sciences", medical);

        LinkedHashMap<String, List<String>> business = new LinkedHashMap<>();
        business.put("Finance & Accounting", Arrays.asList("Financial Analysis", "Tally", "GST", "Excel"));
        business.put("Management", Arrays.asList("Project Management", "Operations", "Supply Chain", "Agile"));
        business.put("Marketing", Arrays.asList("Digital Marketing", "SEO", "Brand Management", "CRM"));
        SKILL_CATEGORIES.put("Business & Commerce", business);

        LinkedHashMap<String, List<String>> arts = new LinkedHashMap<>();
        arts.put("Design", Arrays.asList("UI/UX Design", "Figma", "Photoshop", "Illustrator"));
        arts.put("Media", Arrays.asList("Content Writing", "Video Editing", "Copywriting", "Public Relations"));
        arts.put("Creative Skills", Arrays.asList("Photography", "Animation", "Typography", "Brand Identity"));
        SKILL_CATEGORIES.put("Arts, Design & Media", arts);

        LinkedHashMap<String, List<String>> science = new LinkedHashMap<>();
        science.put("Core Sciences", Arrays.asList("Physics", "Chemistry", "Biology", "Microbiology"));
        science.put("Research Skills", Arrays.asList("Research Methodology", "Statistical Analysis", "SPSS", "Academic Writing"));
        science.put("Applied Sciences", Arrays.asList("Biotechnology", "Nanotechnology", "Forensic Science", "Astronomy"));
        SKILL_CATEGORIES.put("Science & Research", science);

        LinkedHashMap<String, List<String>> law = new LinkedHashMap<>();
        law.put("Legal Skills", Arrays.asList("Contract Law", "Corporate Law", "Legal Research", "Drafting"));
        law.put("Social Sciences", Arrays.asList("Psychology", "Sociology", "Economics", "Public Policy"));
        law.put("Communication", Arrays.asList("Public Speaking", "Negotiation", "Debate", "Report Writing"));
        SKILL_CATEGORIES.put("Law & Social Sciences", law);
    }

    private MaterialButton analyzeButton;
    private ProgressBar progressBar;
    private GridLayout fieldGrid;
    private LinearLayout categoryContainer;
    private LinearLayout skillHeader;
    private View emptyStateCard;
    private TextView skillHeaderText;
    private TextView selectedCountText;
    private TextView selectedPreviewText;
    // Mode tabs
    private MaterialButton tabSkills;
    private MaterialButton tabResume;
    private LinearLayout skillsPanel;
    private View resumePanel;
    private LinearLayout resumeDropZone;
    private LinearLayout resumeFileRow;
    private TextView resumeFileName;
    private TextView resumeFileSize;
    private TextView resumeClearButton;
    private View loginBannerCard;
    private String currentMode = "skills"; // "skills" | "resume"
    private android.net.Uri selectedResumeUri = null;
    private ApiClient apiClient;
    private SessionManager sessionManager;
    private final java.util.LinkedHashSet<String> selectedSkills = new java.util.LinkedHashSet<>();
    private String selectedField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis);

        apiClient = new ApiClient();
        sessionManager = new SessionManager(this);

        // Tabs
        tabSkills   = findViewById(R.id.tabSkills);
        tabResume   = findViewById(R.id.tabResume);
        skillsPanel = findViewById(R.id.skillsPanel);
        resumePanel = findViewById(R.id.resumePanel);
        resumeDropZone   = resumePanel.findViewById(R.id.resumeDropZone);
        resumeFileRow    = resumePanel.findViewById(R.id.resumeFileRow);
        resumeFileName   = resumePanel.findViewById(R.id.resumeFileName);
        resumeFileSize   = resumePanel.findViewById(R.id.resumeFileSize);
        resumeClearButton= resumePanel.findViewById(R.id.resumeClearButton);
        loginBannerCard  = findViewById(R.id.loginBannerCard);
        View loginForResultsBtn = findViewById(R.id.loginForResultsButton);

        analyzeButton      = findViewById(R.id.analyzeButton);
        progressBar        = findViewById(R.id.progressBar);
        fieldGrid          = findViewById(R.id.fieldGrid);
        categoryContainer  = findViewById(R.id.categoryContainer);
        skillHeader        = findViewById(R.id.skillHeader);
        emptyStateCard     = findViewById(R.id.emptyStateCard);
        skillHeaderText    = findViewById(R.id.skillHeaderText);
        selectedCountText  = findViewById(R.id.selectedCountText);
        selectedPreviewText= findViewById(R.id.selectedPreviewText);

        tabSkills.setOnClickListener(v -> setMode("skills"));
        tabResume.setOnClickListener(v -> setMode("resume"));
        resumePickerAreaClick();
        if (resumeClearButton != null) resumeClearButton.setOnClickListener(v -> clearResume());
        if (loginForResultsBtn != null) loginForResultsBtn.setOnClickListener(v -> navigateToLogin());

        analyzeButton.setOnClickListener(v -> analyzeSkills());
        populateFieldCards();
        updateSelectionUi();
        setMode("skills");
    }

    // ─── Mode switching ───────────────────────────────────────────────────────

    private void setMode(String mode) {
        currentMode = mode;
        boolean isSkills = mode.equals("skills");
        skillsPanel.setVisibility(isSkills ? View.VISIBLE : View.GONE);
        resumePanel.setVisibility(isSkills ? View.GONE : View.VISIBLE);
        // Highlight active tab
        tabSkills.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                isSkills ? android.graphics.Color.parseColor("#185FA5")
                         : android.graphics.Color.TRANSPARENT));
        tabSkills.setTextColor(isSkills
                ? android.graphics.Color.WHITE
                : android.graphics.Color.parseColor("#666666"));
        tabResume.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                isSkills ? android.graphics.Color.TRANSPARENT
                         : android.graphics.Color.parseColor("#185FA5")));
        tabResume.setTextColor(isSkills
                ? android.graphics.Color.parseColor("#666666")
                : android.graphics.Color.WHITE);
        updateSelectionUi();
    }

    private void resumePickerAreaClick() {
        // Opens the system file picker for PDFs
        View pickerArea = resumePanel != null ? resumePanel.findViewById(R.id.resumePickerArea) : null;
        if (pickerArea == null) return;
        pickerArea.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            startActivityForResult(intent, 1001);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            selectedResumeUri = data.getData();
            if (selectedResumeUri != null) {
                String name = selectedResumeUri.getLastPathSegment();
                resumeFileName.setText(name != null ? name : "resume.pdf");
                resumeFileSize.setText("Ready to analyze");
                resumeDropZone.setVisibility(View.GONE);
                resumeFileRow.setVisibility(View.VISIBLE);
                updateSelectionUi();
            }
        }
    }

    private void clearResume() {
        selectedResumeUri = null;
        resumeDropZone.setVisibility(View.VISIBLE);
        resumeFileRow.setVisibility(View.GONE);
        updateSelectionUi();
    }

    private void navigateToLogin() {
        startActivity(new android.content.Intent(this, LoginActivity.class));
    }

    // ─── Analyze ──────────────────────────────────────────────────────────────

    private void analyzeSkills() {
        // Validation — mirrors JSX: if skills mode and none selected, return; resume mode and no file, return
        if (currentMode.equals("skills") && selectedSkills.isEmpty() && selectedField == null) {
            Toast.makeText(this, getString(R.string.enter_prompt), Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentMode.equals("resume") && selectedResumeUri == null) {
            Toast.makeText(this, "Please select a PDF file first.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Login-gate: if no token, save pending state and navigate to login
        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            navigateToLogin();
            return;
        }

        setLoading(true);
        String prompt = buildAnalysisPrompt();

        apiClient.analyzeSkills(token, prompt, new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull java.io.IOException e) {
                runOnUiThread(() -> {
                    setLoading(false);
                    Toast.makeText(AnalysisActivity.this, "Analysis failed. Please try again.", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws java.io.IOException {
                String body = response.body() != null ? response.body().string() : "";
                runOnUiThread(() -> {
                    setLoading(false);
                    if (response.isSuccessful()) {
                        Intent intent = new Intent(AnalysisActivity.this, ResultsActivity.class);
                        intent.putExtra("analysis_json", body);
                        startActivity(intent);
                    } else {
                        Toast.makeText(AnalysisActivity.this, "Analysis failed. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        analyzeButton.setEnabled(!loading);
        analyzeButton.setText(loading ? "Analyzing... please wait ⏳" : getAnalyzeButtonText());
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void populateFieldCards() {
        fieldGrid.removeAllViews();
        List<String> fields = new ArrayList<>(SKILL_CATEGORIES.keySet());
        for (String field : fields) {
            MaterialButton button = new MaterialButton(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(0, 0, dp(12), dp(12));
            button.setLayoutParams(params);
            button.setText(field);
            button.setAllCaps(false);
            button.setTextSize(13f);
            button.setCornerRadius(dp(14));
            button.setInsetTop(0);
            button.setInsetBottom(0);
            button.setMinHeight(dp(72));
            button.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            styleFieldButton(button, field.equals(selectedField));
            button.setOnClickListener(v -> {
                selectedField = field;
                selectedSkills.clear();
                populateFieldCards();
                populateSkillCategories();
                updateSelectionUi();
            });
            fieldGrid.addView(button);
        }
    }

    private void populateSkillCategories() {
        categoryContainer.removeAllViews();
        if (selectedField == null) {
            categoryContainer.setVisibility(View.GONE);
            skillHeader.setVisibility(View.GONE);
            emptyStateCard.setVisibility(View.VISIBLE);
            return;
        }

        LinkedHashMap<String, List<String>> categories = SKILL_CATEGORIES.get(selectedField);
        if (categories == null) {
            return;
        }

        skillHeader.setVisibility(View.VISIBLE);
        categoryContainer.setVisibility(View.VISIBLE);
        emptyStateCard.setVisibility(View.GONE);
        skillHeaderText.setText("Select your skills in " + selectedField);

        for (Map.Entry<String, List<String>> entry : categories.entrySet()) {
            CardView card = new CardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.bottomMargin = dp(16);
            card.setLayoutParams(cardParams);
            card.setCardBackgroundColor(Color.WHITE);
            card.setRadius(dp(16));
            card.setCardElevation(0f);
            card.setUseCompatPadding(false);

            LinearLayout content = new LinearLayout(this);
            content.setOrientation(LinearLayout.VERTICAL);
            content.setPadding(dp(20), dp(20), dp(20), dp(20));

            TextView title = new TextView(this);
            title.setText(entry.getKey());
            title.setTextColor(Color.parseColor("#333333"));
            title.setTextSize(14f);
            title.setTypeface(title.getTypeface(), android.graphics.Typeface.BOLD);
            content.addView(title);

            ChipGroup chipGroup = new ChipGroup(this);
            LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            chipParams.topMargin = dp(14);
            chipGroup.setLayoutParams(chipParams);
            chipGroup.setChipSpacingHorizontal(dp(8));
            chipGroup.setChipSpacingVertical(dp(8));

            for (String skill : entry.getValue()) {
                Chip chip = new Chip(this);
                chip.setText(skill);
                chip.setCheckable(true);
                chip.setChecked(selectedSkills.contains(skill));
                chip.setEnsureMinTouchTargetSize(false);
                chip.setChipCornerRadius(dp(10));
                chip.setTextSize(13f);
                styleSkillChip(chip, chip.isChecked());
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedSkills.add(skill);
                    } else {
                        selectedSkills.remove(skill);
                    }
                    styleSkillChip(chip, isChecked);
                    updateSelectionUi();
                });
                chipGroup.addView(chip);
            }

            content.addView(chipGroup);
            card.addView(content);
            categoryContainer.addView(card);
        }
    }

    private void updateSelectionUi() {
        int count = selectedSkills.size();
        // Count badge pill — only shown when count > 0 (mirrors JSX)
        if (selectedCountText != null) {
            selectedCountText.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
            selectedCountText.setText(count + " selected");
        }
        // Preview text below analyze button
        if (selectedPreviewText != null) {
            if (currentMode.equals("skills") && count > 0) {
                selectedPreviewText.setVisibility(View.VISIBLE);
                selectedPreviewText.setText("Selected: " + buildPreviewText());
            } else {
                selectedPreviewText.setVisibility(View.GONE);
            }
        }
        analyzeButton.setText(getAnalyzeButtonText());
        // Login banner: shown only when not logged in AND skills are selected
        if (loginBannerCard != null) {
            boolean notLoggedIn = sessionManager.getToken() == null || sessionManager.getToken().isEmpty();
            boolean hasSelection = currentMode.equals("skills") ? count > 0 : selectedResumeUri != null;
            loginBannerCard.setVisibility(notLoggedIn && hasSelection ? View.VISIBLE : View.GONE);
        }
    }

    private String getAnalyzeButtonText() {
        if (!selectedSkills.isEmpty()) {
            return "Analyze my " + selectedSkills.size() + " skills";
        }
        return "Analyze my profile";
    }

    private String buildPreviewText() {
        List<String> preview = new ArrayList<>(selectedSkills);
        int visibleCount = Math.min(preview.size(), 5);
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < visibleCount; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(preview.get(i));
        }
        if (preview.size() > visibleCount) {
            builder.append(" +").append(preview.size() - visibleCount).append(" more");
        }
        return builder.toString();
    }

    private String buildAnalysisPrompt() {
        String manualNote = promptInput.getText().toString().trim();
        StringBuilder builder = new StringBuilder();
        if (selectedField != null && !selectedField.isEmpty()) {
            builder.append("Field: ").append(selectedField);
        }
        if (!selectedSkills.isEmpty()) {
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append("Skills: ").append(String.join(", ", selectedSkills));
        }
        if (!manualNote.isEmpty()) {
            if (builder.length() > 0) {
                builder.append("\n");
            }
            builder.append(manualNote);
        }
        return builder.toString().trim();
    }

    private void styleFieldButton(MaterialButton button, boolean active) {
        if (active) {
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#E6F1FB")));
            button.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#185FA5")));
            button.setStrokeWidth(dp(2));
            button.setTextColor(Color.parseColor("#185FA5"));
        } else {
            button.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.WHITE));
            button.setStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#EEEEEE")));
            button.setStrokeWidth(dp(1));
            button.setTextColor(Color.parseColor("#333333"));
        }
    }

    private void styleSkillChip(Chip chip, boolean selected) {
        if (selected) {
            chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#E6F1FB")));
            chip.setChipStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#185FA5")));
            chip.setChipStrokeWidth(dp(1));
            chip.setTextColor(Color.parseColor("#185FA5"));
        } else {
            chip.setChipBackgroundColor(android.content.res.ColorStateList.valueOf(Color.WHITE));
            chip.setChipStrokeColor(android.content.res.ColorStateList.valueOf(Color.parseColor("#E5E5E5")));
            chip.setChipStrokeWidth(dp(1));
            chip.setTextColor(Color.parseColor("#555555"));
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private String formatAnalysisResult(JSONObject json) {
        StringBuilder builder = new StringBuilder();
        org.json.JSONArray careerMatches = json.optJSONArray("careerMatches");

        if (careerMatches == null) {
            return "";
        }

        for (int i = 0; i < careerMatches.length(); i++) {
            JSONObject match = careerMatches.optJSONObject(i);
            if (match == null) {
                continue;
            }

            if (builder.length() > 0) {
                builder.append("\n\n");
            }

            String title = match.optString("careerTitle", "Career Match");
            int percentage = match.optInt("matchPercentage", 0);
            builder.append(title).append(" - ").append(percentage).append("% match");

            String description = match.optString("description");
            if (!description.isEmpty()) {
                builder.append("\n").append(description);
            }

            org.json.JSONArray missingSkills = match.optJSONArray("missingSkills");
            if (missingSkills != null && missingSkills.length() > 0) {
                builder.append("\nMissing skills: ");
                for (int j = 0; j < missingSkills.length(); j++) {
                    if (j > 0) {
                        builder.append(", ");
                    }
                    builder.append(missingSkills.optString(j));
                }
            }
        }

        return builder.toString().trim();
    }
}
