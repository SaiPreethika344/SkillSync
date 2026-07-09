package com.skillsync.backend.controller;

import com.skillsync.backend.dto.AnalysisResultDto;
import com.skillsync.backend.service.ResumeAnalysisService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/analysis")
@PreAuthorize("isAuthenticated()")
public class ResumeUploadController {

    private final ResumeAnalysisService resumeAnalysisService;

    public ResumeUploadController(ResumeAnalysisService resumeAnalysisService) {
        this.resumeAnalysisService = resumeAnalysisService;
    }

    @PostMapping(value = "/upload-resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalysisResultDto> uploadResume(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(resumeAnalysisService.analyzeResume(file));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
