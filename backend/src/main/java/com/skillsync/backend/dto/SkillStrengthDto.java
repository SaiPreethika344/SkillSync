package com.skillsync.backend.dto;

public class SkillStrengthDto {

    private String skillName;
    private int percentage;

    public SkillStrengthDto() {
    }

    public SkillStrengthDto(String skillName, int percentage) {
        this.skillName = skillName;
        this.percentage = percentage;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }
}
