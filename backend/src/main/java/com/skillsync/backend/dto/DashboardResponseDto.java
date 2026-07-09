package com.skillsync.backend.dto;

import java.util.List;

public class DashboardResponseDto {

    private String userName;
    private int topCareerMatchScore;
    private List<SkillStrengthDto> skillStrengths;
    private List<DashboardCareerMatchDto> topCareerMatches;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getTopCareerMatchScore() {
        return topCareerMatchScore;
    }

    public void setTopCareerMatchScore(int topCareerMatchScore) {
        this.topCareerMatchScore = topCareerMatchScore;
    }

    public List<SkillStrengthDto> getSkillStrengths() {
        return skillStrengths;
    }

    public void setSkillStrengths(List<SkillStrengthDto> skillStrengths) {
        this.skillStrengths = skillStrengths;
    }

    public List<DashboardCareerMatchDto> getTopCareerMatches() {
        return topCareerMatches;
    }

    public void setTopCareerMatches(List<DashboardCareerMatchDto> topCareerMatches) {
        this.topCareerMatches = topCareerMatches;
    }
}
