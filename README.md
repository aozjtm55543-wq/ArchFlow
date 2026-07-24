# ArchFlow

ArchFlow is an AI-native backend architecture that uses generative AI as an agentic collaborator while keeping service reliability, cost control, and operational observability under direct backend engineering control. Instead of relying on raw model output, the project applies structured DTO contracts, response sanitization, traffic throttling, and latency tracing so LLM behavior can be treated like a production service dependency.

## 🚀 핵심 아키텍처 및 트러블슈팅 (FinOps & DevOps)

### LLM API 비용 보호 및 어뷰징 방어 (FinOps)
Bucket4j 기반 토큰 버킷 알고리즘을 적용한 IP 기반 Rate Limiting을 도입해, 클라이언트 IP당 분당 최대 5회 요청으로 제한합니다. 초과 요청은 즉시 차단되며, `429 Too Many Requests`와 함께 명확한 안내 메시지를 반환합니다.

### 관측 가능성(Observability) 확보
Spring AOP와 `@AiPerformanceTrace`를 활용해 Gemini API 호출의 응답 지연 시간을 비침습적으로 추적합니다. 실행 시간이 8초를 초과하면 경고 로그를 남겨, 느린 AI 호출을 빠르게 식별할 수 있습니다.

### 무중단 운영 및 CI/CD (DevOps)
Render와 같은 무료 클라우드 환경에서의 콜드 스타트 방지를 위해 `GET /api/health` 헬스 체크 엔드포인트를 제공하며, GitHub Actions 파이프라인을 통해 테스트, Docker 빌드, 그리고 Render 배포 훅 트리거까지 자동화했습니다.

## 시스템 검증 및 운영 지표 (Verification Metrics)

| 영역 | 검증 포인트 | 상태 |
| --- | --- | --- |
| 테스트 검증 | JUnit 5 + Mockito 기반 단위/슬라이스 테스트 통과, Rate Limit 차단 및 AOP 인터셉트 검증 완료 | ✅ |
| LLM 응답 통제 | JSON 구조 기반 DTO 매핑, Trailing Comma 및 Markdown 코드 블록 제거 파이프라인 적용 | ✅ |
| 트래픽 및 비용 보호 | Bucket4j 기반 IP당 분당 5회 제한, 초과 시 429 즉시 반환 | ✅ |
| 관측 가능성 | `@AiPerformanceTrace` AOP를 통한 Gemini API 지연 시간 추적, 8초 초과 시 WARN 로그 | ✅ |

> 운영 관점에서 ArchFlow는 단순히 AI 결과를 생성하는 앱이 아니라, LLM의 비정형성을 제어하고 비용·성능·안정성을 관리하는 AI-Native 백엔드 서비스로 설계되었습니다.

## 아키텍처

```text
Browser
  |
  v
Spring Boot Controller
  |
  v
GeminiService
  |-> DTO Mapping / JSON Sanitization
  |-> Rate Limiting Filter
  |-> AOP Latency Tracing
  |-> Self-Reflection Analysis
  |
  +-> Gemini API
```

## 기술 스택

- Java 17
- Spring Boot 3
- Spring Validation
- RestClient
- Jackson
- JUnit 5
- Mockito
- Bucket4j
- Spring AOP
- Tailwind CSS

## 로컬 실행

```bash
./mvnw spring-boot:run
```

브라우저에서 다음 주소로 접속합니다.

```text
http://localhost:8080
```

## Docker 실행

```bash
docker build -t archflow .
docker run -p 8080:8080 -e GEMINI_API_KEY=your_key archflow
```

## 배포 및 운영

Render 또는 Koyeb 같은 컨테이너 배포 환경에 바로 적합하도록 구성되었으며, `GEMINI_API_KEY` 환경 변수와 `GET /api/health` 헬스 체크를 통해 운영 노출이 가능하도록 설계했습니다.

## 아키텍처 결정 기록 (ADR / Troubleshooting)

### ADR 1: 초안 생성 후 Self-Reflection 검토 파이프라인을 채택한 이유
단일 단계로 LLM에게 바로 결과를 요청하면 모델이 환각을 일으키거나 구조적 일관성을 잃을 수 있습니다. 따라서 ArchFlow는 첫 번째 단계에서 설계 초안을 생성하고, 두 번째 단계에서 시니어 아키텍트 관점으로 불일치와 누락 기능을 검토하는 2단계 파이프라인을 도입했습니다. 이 방식은 생성 결과의 품질과 논리적 무결성을 동시에 높이는 데 효과적입니다.

### ADR 2: 외부 AI API 연동의 불안정성을 백엔드에서 방어한 방식
Gemini API는 Rate Limit, 응답 지연, JSON 파싱 오류 등 다양한 운영 리스크를 동반합니다. ArchFlow는 Bucket4j 기반 IP 제한, AOP 기반 응답 지연 추적, JSON Sanitization, 그리고 예외 전용 예외 처리 계층을 통해 이러한 불안정성을 백엔드 서비스 레벨에서 제어하도록 설계했습니다. 이를 통해 LLM 호출이 단순한 외부 의존성으로 남지 않고, 운영 가능한 서비스 컴포넌트로 안정화되었습니다.

## 향후 개선 로드맵 (To-Do / Roadmap)

- [ ] Testcontainers 또는 WireMock을 활용한 외부 AI API 격리 및 통합 테스트 환경 구성
- [ ] Prometheus / Grafana 연동을 통한 AI API 지연 시간 및 토큰 소모량 메트릭 시각화
- [ ] Circuit Breaker(Resilience4j) 도입을 통한 AI 서비스 장애 격리 및 Fallback 응답 구현
