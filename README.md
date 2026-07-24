# ArchFlow

ArchFlow는 Gemini API로 프로젝트 설계 문서를 생성하고 검토하는 AI-Native 서비스입니다. LLM 출력은 신뢰하지 않는다는 전제에서 DTO 계약 검증, 안전한 화면 렌더링, 요청 제한, 타임아웃, 성능 추적 및 표준 오류 응답을 백엔드와 UI에 적용했습니다.

## 🚀 핵심 아키텍처 및 트러블슈팅 (FinOps & DevOps)

### LLM API 비용 보호 및 어뷰징 방어 (FinOps)
Bucket4j 기반 토큰 버킷 알고리즘으로 클라이언트 IP당 분당 최대 5회 요청을 허용합니다. 초과 요청은 `429 Too Many Requests`로 즉시 차단되며, 유휴 IP 버킷은 정리하고 추적 가능한 클라이언트 수도 제한해 메모리 증가를 방지합니다.

### 관측 가능성(Observability) 확보
Spring AOP와 `@AiPerformanceTrace`로 Gemini API 호출 시간을 추적합니다. 8초를 넘으면 경고 로그를 남기며, `RestClient` 연결·응답 타임아웃을 설정해 외부 API 지연이 서버 스레드를 장시간 점유하지 않게 했습니다.

### 무중단 운영 및 CI/CD (DevOps)
Render와 같은 무료 클라우드 환경에서 콜드 스타트 문제를 완화하기 위해 `GET /api/health` 헬스 체크 엔드포인트를 제공하고, GitHub Actions 기반의 테스트·도커 빌드·배포 훅 트리거 파이프라인을 구성했습니다.

## 시스템 검증 및 운영 지표 (Verification Metrics)

| 영역 | 검증 포인트 | 상태 |
| --- | --- | --- |
| 테스트 검증 | JUnit 5와 Mockito 기반으로 컨트롤러 유효성 검증, Gemini 응답 파싱·오류 처리, IP별 요청 제한을 검증합니다. | ✅ |
| LLM 응답 통제 | JSON MIME 응답을 요청하고, 코드 블록 제거 후 DTO 필수 필드를 검증해 계약을 만족하지 않는 응답을 거부합니다. | ✅ |
| 트래픽 및 비용 보호 | Bucket4j 기반 IP당 분당 5회 제한을 적용했고, 초과 시 429 응답을 즉시 반환합니다. | ✅ |
| 관측 가능성 | `@AiPerformanceTrace` AOP를 통해 Gemini API 호출 지연 시간을 추적하고, 8초 초과 시 WARN 로그로 슬로우 API를 감지합니다. | ✅ |

> 운영 관점에서 ArchFlow는 단순히 AI 결과를 생성하는 애플리케이션이 아니라, LLM의 비정형성을 제어하고 비용·성능·안정성을 관리하는 AI-Native 백엔드 서비스로 설계된 프로젝트입니다.

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
- Spring Validation 및 DTO 계약 검증
- RestClient
- Jackson
- JUnit 5
- Mockito
- Bucket4j
- Spring AOP
- Tailwind CSS, HTML 이스케이프 렌더링

## 로컬 실행

```bash
GEMINI_API_KEY=your_key ./mvnw spring-boot:run
```

Windows PowerShell에서는 다음을 사용합니다.

```powershell
$env:GEMINI_API_KEY = "your_key"
.\mvnw.cmd spring-boot:run
```

브라우저에서 다음 주소로 접속할 수 있습니다.

```text
http://localhost:8080
```

## Docker 실행

```bash
docker build -t archflow .
docker run -p 8080:8080 -e GEMINI_API_KEY=your_key archflow
```

## 배포 및 운영

Render 또는 Koyeb 같은 컨테이너 기반 배포 환경에 적합하도록 구성했습니다. `GEMINI_API_KEY`와 선택적 `CORS_ALLOWED_ORIGINS`, `GEMINI_CONNECT_TIMEOUT_MS`, `GEMINI_READ_TIMEOUT_MS` 환경 변수를 설정할 수 있으며, `GET /api/health` 헬스 체크를 제공합니다.

## 아키텍처 결정 기록 (ADR / Troubleshooting)

### ADR 1: 초안 생성 후 Self-Reflection 검토 파이프라인을 채택한 이유
단일 단계로 LLM에게 바로 결과를 요청하면 모델이 환각(Hallucination) 현상을 일으키거나 구조적 일관성을 잃을 수 있습니다. 따라서 ArchFlow는 첫 번째 단계에서 설계 초안을 생성하고, 두 번째 단계에서 시니어 아키텍트 관점으로 불일치와 누락된 기능을 검토하는 2단계 파이프라인을 도입했습니다. 이 구조는 생성 결과의 품질과 논리적 무결성을 동시에 높이는 데 효과적입니다.

### ADR 2: 외부 AI API 연동의 불안정성을 백엔드에서 방어한 방식
Gemini API는 Rate Limit, 응답 지연, JSON 파싱 오류 등 다양한 운영 리스크를 동반합니다. ArchFlow는 Bucket4j 기반 IP 제한, AOP 기반 응답 지연 추적, DTO 계약 검증, 연결·응답 타임아웃, 그리고 예외 처리 계층으로 이를 제어합니다. 브라우저도 AI가 반환한 문자열을 HTML로 신뢰하지 않고 이스케이프 처리합니다.

## 향후 개선 로드맵 (To-Do / Roadmap)

- [ ] WireMock을 활용한 외부 AI API 격리 및 통합 테스트 환경 구성
- [ ] Prometheus / Grafana 연동을 통한 AI API 지연 시간 및 토큰 소모량 메트릭 시각화
- [ ] Circuit Breaker(Resilience4j) 도입을 통한 AI 서비스 장애 격리 및 Fallback 응답 구현
