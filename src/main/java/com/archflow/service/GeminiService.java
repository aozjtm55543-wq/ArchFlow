package com.archflow.service;

import com.archflow.annotation.AiPerformanceTrace;
import com.archflow.dto.request.ProjectAnalyzeRequest;
import com.archflow.dto.request.ProjectGenerateRequest;
import com.archflow.dto.response.ProjectAnalyzeResponse;
import com.archflow.dto.response.ProjectBlueprintResponse;
import com.archflow.exception.ExternalApiException;
import com.archflow.exception.InvalidAiResponseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Service
public class GeminiService {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String url;

    public GeminiService(
        @Value("${gemini.api-key}") String apiKey,
        @Value("${gemini.url}") String url,
        ObjectMapper objectMapper
    ) {
        this(apiKey, url, objectMapper, RestClient.builder().build());
    }

    public GeminiService(String apiKey, String url, ObjectMapper objectMapper, RestClient restClient) {
        this.apiKey = apiKey;
        this.url = url;
        this.objectMapper = objectMapper;
        this.restClient = restClient;
    }

    @AiPerformanceTrace
    public ProjectBlueprintResponse generateBlueprint(ProjectGenerateRequest request) {
        String systemPrompt = "당신은 실무 경험이 풍부한 시니어 소프트웨어 아키텍트입니다. 사용자의 입력을 분석하여 현실적이고 일관성 있는 개발 문서를 지정된 JSON 규격으로만 작성하십시오.";
        String userPrompt = buildUserPrompt(request);

        Map<String, Object> payload = Map.of(
            "systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))),
            "contents", List.of(Map.of("parts", List.of(Map.of("text", userPrompt)))),
            "generationConfig", Map.of(
                "responseMimeType", "application/json",
                "temperature", 0.2
            )
        );

        try {
            String responseBody = callGemini(payload);
            String generatedText = extractGeneratedText(responseBody);
            String sanitizedJson = sanitizeResponse(generatedText);
            return objectMapper.readValue(sanitizedJson, ProjectBlueprintResponse.class);
        } catch (ExternalApiException exception) {
            throw exception;
        } catch (JsonProcessingException exception) {
            throw new InvalidAiResponseException("AI response could not be parsed into ProjectBlueprintResponse", exception);
        } catch (RestClientException exception) {
            throw new ExternalApiException("Gemini API call failed", exception);
        }
    }

    @AiPerformanceTrace
    public ProjectAnalyzeResponse analyzeBlueprint(ProjectAnalyzeRequest request) {
        String systemPrompt = "당신은 냉철하고 비판적인 시니어 소프트웨어 아키텍트입니다. 제공된 프로젝트 설계 문서를 엄격하게 검토하고, 아키텍처 결함 및 개선점을 지정된 JSON 규격으로만 출력하십시오.";
        String userPrompt = buildAnalyzePrompt(request);

        Map<String, Object> payload = Map.of(
            "systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))),
            "contents", List.of(Map.of("parts", List.of(Map.of("text", userPrompt)))),
            "generationConfig", Map.of(
                "responseMimeType", "application/json",
                "temperature", 0.2
            )
        );

        try {
            String responseBody = callGemini(payload);
            String generatedText = extractGeneratedText(responseBody);
            String sanitizedJson = sanitizeResponse(generatedText);
            return objectMapper.readValue(sanitizedJson, ProjectAnalyzeResponse.class);
        } catch (ExternalApiException exception) {
            throw exception;
        } catch (JsonProcessingException exception) {
            throw new InvalidAiResponseException("AI response could not be parsed into ProjectAnalyzeResponse", exception);
        } catch (RestClientException exception) {
            throw new ExternalApiException("Gemini API call failed", exception);
        }
    }

    private String buildUserPrompt(ProjectGenerateRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append("다음 요구사항을 기반으로 프로젝트 개발 문서를 생성하십시오.\n");
        builder.append("프로젝트 이름: ").append(request.projectName()).append("\n");
        builder.append("설명: ").append(request.description()).append("\n");
        builder.append("기술 스택: ").append(request.techStack()).append("\n");
        builder.append("필수 기능: ");
        if (request.requiredFeatures() == null || request.requiredFeatures().isEmpty()) {
            builder.append("없음");
        } else {
            builder.append(String.join(", ", request.requiredFeatures()));
        }
        builder.append("\n\n");
        builder.append("반드시 다음 JSON 구조를 준수하십시오: projectSummary, readme, apiSpecifications, databaseSchema, directoryStructure, developmentChecklist");
        return builder.toString();
    }

    private String buildAnalyzePrompt(ProjectAnalyzeRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append("다음 프로젝트 설계 문서를 검토하십시오.\n");
        builder.append("반드시 다음 JSON 구조를 준수하십시오: consistencyIssues, recommendedMissingFeatures, difficultyLevel, technicalBottleneck\n\n");
        builder.append("설계 문서: ");
        try {
            builder.append(objectMapper.writeValueAsString(request.projectBlueprint()));
        } catch (JsonProcessingException exception) {
            throw new InvalidAiResponseException("Failed to serialize project blueprint for analysis", exception);
        }
        return builder.toString();
    }

    private String callGemini(Map<String, Object> payload) {
        return restClient.post()
            .uri(uriBuilder -> uriBuilder
                .scheme("https")
                .host("generativelanguage.googleapis.com")
                .path(url.replace("https://generativelanguage.googleapis.com", ""))
                .queryParam("key", apiKey)
                .build())
            .contentType(MediaType.APPLICATION_JSON)
            .body(payload)
            .retrieve()
            .onStatus(status -> status.isError(), (requestMessage, responseMessage) -> {
                throw new ExternalApiException("Gemini API returned error status " + responseMessage.getStatusCode());
            })
            .body(String.class);
    }

    private String extractGeneratedText(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
            String text = textNode.asText();
            if (text == null || text.isBlank()) {
                throw new InvalidAiResponseException("AI response did not contain generated text", null);
            }
            return text;
        } catch (JsonProcessingException exception) {
            throw new InvalidAiResponseException("AI response could not be parsed", exception);
        }
    }

    private String sanitizeResponse(String rawResponse) {
        String trimmed = rawResponse == null ? "" : rawResponse.trim();
        if (trimmed.startsWith("```")) {
            int fenceEnd = trimmed.indexOf("\n", 3);
            String content = fenceEnd >= 0 ? trimmed.substring(fenceEnd).trim() : trimmed;
            int start = content.indexOf('{');
            int end = content.lastIndexOf('}');
            if (start >= 0 && end > start) {
                return content.substring(start, end + 1);
            }
            return content.replaceAll("^```(?:json)?\\s*", "").replaceAll("\\s*```$", "");
        }
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }
}
