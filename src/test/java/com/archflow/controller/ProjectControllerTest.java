package com.archflow.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archflow.dto.request.ProjectGenerateRequest;
import com.archflow.dto.response.ProjectAnalyzeResponse;
import com.archflow.dto.response.ProjectBlueprintResponse;
import com.archflow.service.GeminiService;
import com.archflow.service.RateLimitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GeminiService geminiService;

    @MockBean
    private RateLimitService rateLimitService;

    @BeforeEach
    void allowRequestsThroughRateLimitFilter() {
        when(rateLimitService.allowRequest(any())).thenReturn(true);
    }

    @Test
    void generateShouldReturnOkResponse() throws Exception {
        ProjectBlueprintResponse response = new ProjectBlueprintResponse(
            new ProjectBlueprintResponse.ProjectSummaryDto("Sample", "Description", "Reason"),
            new ProjectBlueprintResponse.ReadmeDto("Overview", List.of("Feature"), List.of("Run")),
            List.of(),
            List.of(),
            List.of(),
            List.of()
        );

        when(geminiService.generateBlueprint(any(ProjectGenerateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ProjectGenerateRequest("Sample", "Description", "Spring Boot", List.of("Feature")))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.projectSummary.title").value("Sample"));
    }

    @Test
    void analyzeShouldReturnOkResponse() throws Exception {
        ProjectAnalyzeResponse response = new ProjectAnalyzeResponse(
            List.of("Issue"),
            List.of("Security"),
            "중",
            "성능"
        );

        when(geminiService.analyzeBlueprint(any())).thenReturn(response);

        mockMvc.perform(post("/api/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"projectBlueprint\":{\"projectSummary\":{\"title\":\"Sample\",\"description\":\"Desc\",\"architectureReasoning\":\"Reason\"},\"readme\":{\"overview\":\"Overview\",\"features\":[\"Feature\"],\"gettingStarted\":[\"Run\"]},\"apiSpecifications\":[],\"databaseSchema\":[],\"directoryStructure\":[],\"developmentChecklist\":[]}}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.difficultyLevel").value("중"));
    }

    @Test
    void invalidRequestShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"description\":\"Missing required fields\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void analyzeWithoutBlueprintShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void generateRejectsOversizedPromptInput() throws Exception {
        ProjectGenerateRequest request = new ProjectGenerateRequest("x".repeat(101), "Description", "Spring Boot", List.of());

        mockMvc.perform(post("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }
}
