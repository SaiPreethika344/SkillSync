package com.skillsync.backend.dto;

public class JobListingDto {

    private String title;
    private String company;
    private String salary;
    private String location;

    public JobListingDto() {
    }

    public JobListingDto(String title, String company, String salary, String location) {
        this.title = title;
        this.company = company;
        this.salary = salary;
        this.location = location;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getSalary() {
        return salary;
    }

    public void setSalary(String salary) {
        this.salary = salary;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
