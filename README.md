# ArchFlow

ArchFlow is an AI-native backend project that turns a short product brief into a structured software blueprint. It combines a Spring Boot service, Gemini-based generation logic, and a self-reflection analysis pipeline to produce documentation that is both human-readable and machine-structured.

## What this project demonstrates

This project is designed to showcase how an AI-powered backend can be built with production-minded engineering discipline.

### 1. Structured output control for LLM responses
A major engineering challenge in AI-native systems is that language models often return free-form text that is inconsistent, verbose, or malformed. ArchFlow addresses this by using Java record DTOs as strict output contracts and mapping Gemini responses into strongly typed Java objects. This makes the AI output predictable and reduces backend parsing failures.

### 2. Robust response sanitization and defensive error handling
Gemini can return markdown fences, trailing commas, or partially invalid JSON. The service layer cleans the response text before parsing and catches parsing and API failures so the application can return controlled errors instead of crashing with unhandled 500s.

### 3. Self-reflection pipeline for architecture validation
After the first generation phase, the application sends the generated design document back to the model for a second-pass review. This self-reflection step checks for consistency issues, recommends missing features, and highlights expected technical bottlenecks.

### 4. Test isolation with Mockito
External LLM calls are mocked in unit and web-layer tests so that CI pipelines remain stable, fast, and cost-free. This makes the service layer testable without relying on a live Gemini API.

## Architecture

```text
Client Browser
    | 
    v
Spring Boot Controller
    | 
    v
GeminiService
    | 
    +--> Gemini API (generateContent)
    |
    +--> JSON Sanitization / DTO Mapping
    |
    +--> Self-Reflection Analysis
```

## Tech stack

- Java 17
- Spring Boot 3
- Spring Validation
- RestClient
- Jackson
- JUnit 5
- Mockito
- Tailwind CSS (frontend UI via static HTML)

## Running locally

```bash
./mvnw spring-boot:run
```

Then open:

```text
http://localhost:8080
```

## Docker

```bash
docker build -t archflow .
docker run -p 8080:8080 -e GEMINI_API_KEY=your_key archflow
```

## Deployment notes

This project is prepared for container-based deployment on platforms such as Render or Koyeb. The Docker image exposes port 8080 and expects the Gemini API key to be provided via the environment variable `GEMINI_API_KEY`.
