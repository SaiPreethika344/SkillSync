package com.skillsync.backend.controller;

import com.skillsync.backend.dto.RoadmapStepDto;
import com.skillsync.backend.service.RoadmapService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/roadmap")
@PreAuthorize("isAuthenticated()")
public class RoadmapController {

    private final RoadmapService roadmapService;

    public RoadmapController(RoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    @GetMapping
    public ResponseEntity<List<RoadmapStepDto>> getRoadmap() {
        return ResponseEntity.ok(roadmapService.getRoadmapForCurrentUser());
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<RoadmapStepDto> markComplete(@PathVariable Long id) {
        return ResponseEntity.ok(roadmapService.markStepComplete(id));
    }

    @PutMapping("/{id}/undo")
    public ResponseEntity<RoadmapStepDto> undoComplete(@PathVariable Long id) {
        return ResponseEntity.ok(roadmapService.undoStepComplete(id));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
