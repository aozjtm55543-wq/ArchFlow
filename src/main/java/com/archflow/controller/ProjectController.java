package com.archflow.controller;

import com.archflow.dto.request.ProjectAnalyzeRequest;
import com.archflow.dto.request.ProjectGenerateRequest;
import com.archflow.dto.response.ProjectAnalyzeResponse;
import com.archflow.dto.response.ProjectBlueprintResponse;
import com.archflow.service.GeminiService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProjectController {
    private final GeminiService geminiService;

    public ProjectController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/generate")
    public ResponseEntity<ProjectBlueprintResponse> generate(@Valid @RequestBody ProjectGenerateRequest request) {
        ProjectBlueprintResponse response = geminiService.generateBlueprint(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/analyze")
    public ResponseEntity<ProjectAnalyzeResponse> analyze(@Valid @RequestBody ProjectAnalyzeRequest request) {
        ProjectAnalyzeResponse response = geminiService.analyzeBlueprint(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
