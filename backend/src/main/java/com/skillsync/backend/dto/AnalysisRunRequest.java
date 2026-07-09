package com.skillsync.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class AnalysisRunRequest {

    @NotBlank(message = "Field is required")
    private String field;

    @NotEmpty(message = "At least one skill is required")
    private List<String> skills;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }
}
