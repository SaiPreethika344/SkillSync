package com.skillsync.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RoadmapStepDto {

    private Long id;
    private String title;
    private boolean isComplete;
    private int stepOrder;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @JsonProperty("isComplete")
    public boolean isComplete() {
        return isComplete;
    }

    @JsonProperty("isComplete")
    public void setComplete(boolean complete) {
        this.isComplete = complete;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public void setStepOrder(int stepOrder) {
        this.stepOrder = stepOrder;
    }
}
