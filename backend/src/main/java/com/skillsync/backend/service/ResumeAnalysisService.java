package com.skillsync.backend.service;

import com.skillsync.backend.dto.AnalysisResultDto;
import com.skillsync.backend.dto.AnalysisRunRequest;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ResumeAnalysisService {

    private final CareerMappingService careerMappingService;
    private final AnalysisService analysisService;

    public ResumeAnalysisService(CareerMappingService careerMappingService, AnalysisService analysisService) {
        this.careerMappingService = careerMappingService;
        this.analysisService = analysisService;
    }

    public AnalysisResultDto analyzeResume(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("PDF file is required");
        }
        if (file.getOriginalFilename() == null
                || !file.getOriginalFilename().toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            throw new IllegalArgumentException("Only PDF files are supported");
        }

        String text = extractText(file);
        List<String> extractedSkills = extractSkillsFromText(text);
        if (extractedSkills.isEmpty()) {
            throw new IllegalArgumentException("No recognizable skills found in resume");
        }

        AnalysisRunRequest request = new AnalysisRunRequest();
        request.setField("Resume Upload");
        request.setSkills(extractedSkills);
        return analysisService.runAnalysis(request);
    }

    private String extractText(MultipartFile file) {
        try (InputStream is = file.getInputStream(); PDDocument document = Loader.loadPDF(is.readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to read PDF content");
        }
    }

    private List<String> extractSkillsFromText(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String normalizedText = text.toLowerCase(Locale.ROOT);
        Set<String> knownSkills = careerMappingService.getAllKnownSkills();

        List<String> found = new ArrayList<>();
        for (String skill : knownSkills) {
            String needle = skill.toLowerCase(Locale.ROOT);
            if (normalizedText.contains(needle)) {
                found.add(skill);
            }
        }
        return found;
    }
}
