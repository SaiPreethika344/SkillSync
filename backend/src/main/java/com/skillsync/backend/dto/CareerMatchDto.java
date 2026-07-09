package com.skillsync.backend.dto;

import java.util.List;

public class CareerMatchDto {

    private String careerTitle;
    private int matchPercentage;
    private String description;
    private List<String> missingSkills;

    public CareerMatchDto() {
    }

    public CareerMatchDto(String careerTitle, int matchPercentage) {
        this.careerTitle = careerTitle;
        this.matchPercentage = matchPercentage;
        this.description = "";
        this.missingSkills = List.of();
    }

    public String getCareerTitle() {
        return careerTitle;
    }

    public void setCareerTitle(String careerTitle) {
        this.careerTitle = careerTitle;
    }

    public int getMatchPercentage() {
        return matchPercentage;
    }

    public void setMatchPercentage(int matchPercentage) {
        this.matchPercentage = matchPercentage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(List<String> missingSkills) {
        this.missingSkills = missingSkills;
    }
}
