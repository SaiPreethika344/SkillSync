package com.skillsync.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AnalysisHistoryItemDto {

    private Long id;
    private String field;
    private List<String> skills;
    private AnalysisResultDto result;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public AnalysisResultDto getResult() {
        return result;
    }

    public void setResult(AnalysisResultDto result) {
        this.result = result;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
