package com.skillsync.backend.controller;

import com.skillsync.backend.dto.AnalysisHistoryItemDto;
import com.skillsync.backend.dto.AnalysisResultDto;
import com.skillsync.backend.dto.AnalysisRunRequest;
import com.skillsync.backend.service.AnalysisService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analysis")
@PreAuthorize("isAuthenticated()")
public class AnalysisController {

    private final AnalysisService analysisService;

    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @PostMapping("/run")
    public ResponseEntity<AnalysisResultDto> runAnalysis(@Valid @RequestBody AnalysisRunRequest request) {
        return ResponseEntity.ok(analysisService.runAnalysis(request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<AnalysisHistoryItemDto>> getHistory() {
        return ResponseEntity.ok(analysisService.getHistory());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
