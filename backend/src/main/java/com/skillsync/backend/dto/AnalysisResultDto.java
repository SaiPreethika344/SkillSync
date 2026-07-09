package com.skillsync.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

public class AnalysisResultDto {

    private List<CareerMatchDto> careerMatches;
    @JsonIgnore
    private List<String> skillGaps;
    @JsonIgnore
    private List<String> roadmapSteps;

    public List<CareerMatchDto> getCareerMatches() {
        return careerMatches;
    }

    public void setCareerMatches(List<CareerMatchDto> careerMatches) {
        this.careerMatches = careerMatches;
    }

    public List<String> getSkillGaps() {
        return skillGaps;
    }

    public void setSkillGaps(List<String> skillGaps) {
        this.skillGaps = skillGaps;
    }

    public List<String> getRoadmapSteps() {
        return roadmapSteps;
    }

    public void setRoadmapSteps(List<String> roadmapSteps) {
        this.roadmapSteps = roadmapSteps;
    }
}
