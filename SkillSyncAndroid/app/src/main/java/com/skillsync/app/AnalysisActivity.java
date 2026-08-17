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
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
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
        // ── Engineering & Technology ─────────────────────────────────────────
        // Source: AnalysisPage.jsx skillCategories['Engineering & Technology']
        LinkedHashMap<String, List<String>> engineering = new LinkedHashMap<>();
        engineering.put("Programming Languages", Arrays.asList(
                "Python", "Java", "C", "C++", "JavaScript", "TypeScript",
                "R", "MATLAB", "Swift", "Kotlin"));
        engineering.put("Web Development", Arrays.asList(
                "React", "Node.js", "HTML/CSS", "Angular", "Vue.js",
                "Django", "Flask", "REST APIs"));
        engineering.put("Data & AI", Arrays.asList(
                "Machine Learning", "Deep Learning", "Data Analysis", "SQL",
                "Power BI", "Tableau", "OpenCV", "NLP"));
        engineering.put("Core Engineering", Arrays.asList(
                "Circuit Design", "AutoCAD", "SolidWorks", "Embedded Systems",
                "IoT", "Robotics", "VLSI"));
        engineering.put("Soft Skills", Arrays.asList(
                "Problem Solving", "Critical Thinking", "Teamwork", "Communication",
                "Time Management", "Leadership", "Adaptability", "Presentation Skills"));
        SKILL_CATEGORIES.put("Engineering & Technology", engineering);

        // ── Medical & Health Sciences ────────────────────────────────────────
        // Source: AnalysisPage.jsx skillCategories['Medical & Health Sciences']
        LinkedHashMap<String, List<String>> medical = new LinkedHashMap<>();
        medical.put("Clinical Skills", Arrays.asList(
                "Patient Assessment", "ECG Reading", "Clinical Diagnosis",
                "First Aid", "Pharmacology", "Anatomy"));
        medical.put("Research & Lab", Arrays.asList(
                "Lab Techniques", "Research Methodology", "Medical Writing",
                "Clinical Trials", "Pathology"));
        medical.put("Healthcare Tech", Arrays.asList(
                "Electronic Health Records", "Medical Imaging",
                "Telemedicine", "Hospital Management"));
        medical.put("Soft Skills", Arrays.asList(
                "Patient Communication", "Medical Ethics",
                "Team Collaboration", "Leadership"));
        medical.put("Professional Skills", Arrays.asList(
                "Empathy", "Communication", "Critical Thinking", "Teamwork",
                "Ethics", "Time Management", "Stress Management", "Leadership"));
        SKILL_CATEGORIES.put("Medical & Health Sciences", medical);

        // ── Business & Commerce ──────────────────────────────────────────────
        // Source: AnalysisPage.jsx skillCategories['Business & Commerce']
        LinkedHashMap<String, List<String>> business = new LinkedHashMap<>();
        business.put("Finance & Accounting", Arrays.asList(
                "Financial Analysis", "Tally", "GST", "Auditing",
                "Taxation", "Cost Accounting", "Excel"));
        business.put("Management", Arrays.asList(
                "Project Management", "Operations", "Supply Chain",
                "Business Strategy", "Agile / Scrum"));
        business.put("Marketing", Arrays.asList(
                "Digital Marketing", "SEO", "Content Marketing",
                "Social Media", "Brand Management", "CRM"));
        business.put("CA / CS Specific", Arrays.asList(
                "Company Law", "IFRS", "Internal Audit",
                "Risk Management", "Financial Reporting"));
        business.put("Soft Skills", Arrays.asList(
                "Negotiation", "Communication", "Leadership", "Critical Thinking",
                "Time Management", "Problem Solving", "Networking", "Presentation Skills"));
        SKILL_CATEGORIES.put("Business & Commerce", business);

        // ── Arts, Design & Media ─────────────────────────────────────────────
        // Source: AnalysisPage.jsx skillCategories['Arts, Design & Media']
        LinkedHashMap<String, List<String>> arts = new LinkedHashMap<>();
        arts.put("Design", Arrays.asList(
                "UI/UX Design", "Figma", "Adobe XD", "Photoshop",
                "Illustrator", "Motion Graphics", "3D Modelling"));
        arts.put("Media & Communication", Arrays.asList(
                "Content Writing", "Journalism", "Video Editing",
                "Public Relations", "Copywriting"));
        arts.put("Fine Arts", Arrays.asList(
                "Photography", "Animation", "Illustration",
                "Typography", "Brand Identity"));
        arts.put("Performing Arts", Arrays.asList(
                "Music", "Acting", "Dance", "Scriptwriting", "Event Management"));
        arts.put("Soft Skills", Arrays.asList(
                "Creativity", "Communication", "Storytelling", "Collaboration",
                "Time Management", "Presentation Skills", "Critical Thinking", "Adaptability"));
        SKILL_CATEGORIES.put("Arts, Design & Media", arts);

        // ── Science & Research ───────────────────────────────────────────────
        // Source: AnalysisPage.jsx skillCategories['Science & Research']
        LinkedHashMap<String, List<String>> science = new LinkedHashMap<>();
        science.put("Core Sciences", Arrays.asList(
                "Physics", "Chemistry", "Biology", "Biochemistry",
                "Microbiology", "Environmental Science"));
        science.put("Research Skills", Arrays.asList(
                "Research Methodology", "Statistical Analysis", "SPSS",
                "Academic Writing", "Lab Skills"));
        science.put("Applied Sciences", Arrays.asList(
                "Biotechnology", "Nanotechnology", "Forensic Science",
                "Geoscience", "Astronomy"));
        science.put("Soft Skills", Arrays.asList(
                "Research Communication", "Critical Thinking", "Teamwork",
                "Grant Writing", "Presentation Skills", "Time Management", "Problem Solving"));
        SKILL_CATEGORIES.put("Science & Research", science);

        // ── Law & Social Sciences ────────────────────────────────────────────
        // Source: AnalysisPage.jsx skillCategories['Law & Social Sciences']
        LinkedHashMap<String, List<String>> law = new LinkedHashMap<>();
        law.put("Legal Skills", Arrays.asList(
                "Contract Law", "Corporate Law", "Criminal Law",
                "Legal Research", "Moot Court", "Drafting"));
        law.put("Social Sciences", Arrays.asList(
                "Psychology", "Sociology", "Economics",
                "Political Science", "Public Policy"));
        law.put("Communication", Arrays.asList(
                "Public Speaking", "Negotiation", "Critical Thinking",
                "Debate", "Report Writing"));
        law.put("Soft Skills", Arrays.asList(
                "Argumentation", "Empathy", "Communication", "Critical Thinking",
                "Leadership", "Time Management", "Ethics", "Negotiation"));
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

        // ── Resume mode: read PDF bytes and POST to /analysis/upload-resume ──
        // Mirrors web api.js uploadResume(): formData.append('file', file)
        //   → fetch('/analysis/upload-resume', { method:'POST', body: formData })
        if (currentMode.equals("resume")) {
            byte[] pdfBytes;
            try {
                try (InputStream is = getContentResolver().openInputStream(selectedResumeUri);
                     ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    if (is == null) throw new IOException("Cannot open URI: " + selectedResumeUri);
                    byte[] buf = new byte[8192];
                    int read;
                    while ((read = is.read(buf)) != -1) {
                        baos.write(buf, 0, read);
                    }
                    pdfBytes = baos.toByteArray();
                }
            } catch (IOException e) {
                setLoading(false);
                Toast.makeText(this, "Could not read the selected PDF file.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Derive a clean filename from the URI for the multipart Content-Disposition
            String filename = selectedResumeUri.getLastPathSegment();
            if (filename == null || filename.isEmpty()) filename = "resume.pdf";
            if (!filename.toLowerCase().endsWith(".pdf")) filename += ".pdf";

            final byte[] bytes = pdfBytes;
            final String name = filename;
            apiClient.uploadResume(token, bytes, name, new okhttp3.Callback() {
                @Override
                public void onFailure(@NonNull okhttp3.Call call, @NonNull java.io.IOException e) {
                    runOnUiThread(() -> {
                        setLoading(false);
                        Toast.makeText(AnalysisActivity.this,
                                "Resume upload failed. Check your connection.", Toast.LENGTH_SHORT).show();
                    });
                }

                @Override
                public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response)
                        throws java.io.IOException {
                    String body = response.body() != null ? response.body().string() : "";
                    runOnUiThread(() -> {
                        setLoading(false);
                        if (response.isSuccessful()) {
                            Intent intent = new Intent(AnalysisActivity.this, ResultsActivity.class);
                            intent.putExtra("analysis_json", body);
                            startActivity(intent);
                        } else {
                            String msg = body.isEmpty() ? "Resume analysis failed." : body;
                            Toast.makeText(AnalysisActivity.this, msg, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            });
            return; // do NOT fall through to skills-mode path
        }

        // ── Skills mode: build prompt string → POST to /analysis/run (unchanged) ──
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
