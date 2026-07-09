package com.skillsync.backend.controller;

import com.skillsync.backend.dto.JobListingDto;
import com.skillsync.backend.service.JobsService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
@PreAuthorize("isAuthenticated()")
public class JobsController {

    private final JobsService jobsService;

    public JobsController(JobsService jobsService) {
        this.jobsService = jobsService;
    }

    @GetMapping
    public ResponseEntity<List<JobListingDto>> getJobs(Authentication authentication) {
        return ResponseEntity.ok(jobsService.getLiveJobsForUser(authentication.getName()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleServiceError(IllegalStateException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
