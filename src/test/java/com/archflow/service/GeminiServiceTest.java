package com.archflow.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.archflow.dto.request.ProjectAnalyzeRequest;
import com.archflow.dto.request.ProjectGenerateRequest;
import com.archflow.dto.response.ProjectAnalyzeResponse;
import com.archflow.dto.response.ProjectBlueprintResponse;
import com.archflow.exception.InvalidAiResponseException;
import com.archflow.exception.AiConfigurationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class GeminiServiceTest {
    private GeminiService geminiService;
    private RestClient restClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        restClient = mock(RestClient.class);
        geminiService = new GeminiService("test-key", "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent", objectMapper, restClient);
    }

    @Test
    void generateBlueprintShouldParseSuccessfulResponse() throws Exception {
        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.<Object[]>any())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(org.mockito.ArgumentMatchers.<Object>any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        String blueprintJson = """
            {"projectSummary":{"title":"Test App","description":"A sample app","architectureReasoning":"Good design"},"readme":{"overview":"Overview","features":["Auth"],"gettingStarted":["Run app"]},"apiSpecifications":[],"databaseSchema":[],"directoryStructure":[],"developmentChecklist":[]}
            """;
        when(responseSpec.body(String.class)).thenReturn("{\"candidates\":[{\"content\":{\"parts\":[{\"text\":"
            + objectMapper.writeValueAsString(blueprintJson) + "}]}}]}");

        ProjectGenerateRequest request = new ProjectGenerateRequest("Test App", "A sample app", "Spring Boot", List.of("Auth"));
        ProjectBlueprintResponse response = geminiService.generateBlueprint(request);

        assertEquals("Test App", response.projectSummary().title());
        assertEquals("Overview", response.readme().overview());
    }

    @Test
    void analyzeBlueprintShouldThrowInvalidAiResponseExceptionWhenJsonIsMalformed() throws Exception {
        RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.<Object[]>any())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.body(org.mockito.ArgumentMatchers.<Object>any())).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        String malformedJson = "{\"consistencyIssues\": [\"A\"], \"recommendedMissingFeatures\": [\"B\"], \"difficultyLevel\": \"중\", \"technicalBottleneck\": \"C\"";
        when(responseSpec.body(String.class)).thenReturn("{\"candidates\":[{\"content\":{\"parts\":[{\"text\":"
            + objectMapper.writeValueAsString(malformedJson) + "}]}}]}");

        ProjectAnalyzeRequest request = new ProjectAnalyzeRequest(new ProjectBlueprintResponse(
            new ProjectBlueprintResponse.ProjectSummaryDto("Test", "Desc", "Reason"),
            new ProjectBlueprintResponse.ReadmeDto("Overview", List.of(), List.of()),
            List.of(),
            List.of(),
            List.of(),
            List.of()
        ));

        assertThrows(InvalidAiResponseException.class, () -> geminiService.analyzeBlueprint(request));
    }

    @Test
    void generateBlueprintShouldRejectMissingApiKeyBeforeCallingGemini() {
        GeminiService serviceWithoutApiKey = new GeminiService("", "https://example.test/generate", objectMapper, restClient);
        ProjectGenerateRequest request = new ProjectGenerateRequest("Test App", "A sample app", "Spring Boot", List.of());

        assertThrows(AiConfigurationException.class, () -> serviceWithoutApiKey.generateBlueprint(request));
    }
}
