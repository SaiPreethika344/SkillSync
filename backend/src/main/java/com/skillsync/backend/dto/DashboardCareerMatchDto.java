package com.skillsync.backend.dto;

public class DashboardCareerMatchDto {

    private String careerTitle;
    private int matchPercentage;
    private String description;

    public DashboardCareerMatchDto() {
    }

    public DashboardCareerMatchDto(String careerTitle, int matchPercentage, String description) {
        this.careerTitle = careerTitle;
        this.matchPercentage = matchPercentage;
        this.description = description;
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
}
