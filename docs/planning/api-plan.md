# Weiver Landing API 기획

2026-09-28

weiver 데모 테스터 모집 랜딩 페이지를 지원하는 Spring Boot API의 구현 계획입니다.

## 목적 및 범위

사전예약 수집과 방문자 행동 트래킹을 처리하는 서버사이드 API를 제공한다. 브라우저가 Supabase에 직접 접근하는 것을 차단하고, 개인정보(이메일)를 안전하게 보관하는 것이 핵심 역할이다.

**범위 안**
- 쿠키 기반 방문자 식별 및 세션 생성
- 익명 이벤트(`page_view`, `reserve_opened`, `reservation_completed`) 저장
- 사전예약 저장 및 중복 방지
- 일별 퍼널 지표 집계 (관리자 전용)

**범위 밖**
- 이메일 발송, 알림 등 외부 연동
- 사용자 인증/계정 관리
- 프론트엔드 렌더링

## 기술 스택

| 구분 | 선택 | 이유 |
| --- | --- | --- |
| 런타임 | Java 21 + Spring Boot 3.5 | LTS, Virtual Thread 지원 |
| 빌드 | Gradle Kotlin DSL | 기존 `build.gradle.kts` 유지 |
| 데이터 접근 | `JdbcClient` (Spring 6.1 내장) | 추가 의존성 없음, 스키마 충돌 방지 |
| DB | Supabase (Postgres 15) | 마이그레이션은 Supabase CLI로 분리 |
| 유효성 검사 | `spring-boot-starter-validation` | Bean Validation 3.0 |
| 모니터링 | Spring Actuator (`/health`, `/info`) | 최소한 노출 |

JPA는 도입하지 않는다. 스키마가 Supabase migration으로 고정되어 있고 쿼리가 단순하므로 `JdbcClient`만으로 충분하다.

## API 엔드포인트 명세

| 메서드 | 경로 | 설명 | 성공 응답 | 실패 응답 |
| --- | --- | --- | --- | --- |
| `POST` | `/api/v1/visitors/bootstrap` | 방문자 UUID 발급/갱신, `HttpOnly` 쿠키 세팅 | `200 OK` | — |
| `POST` | `/api/v1/events` | 익명 이벤트 저장 | `200 OK` | 실패 시도 응답도 `200` (fire-and-forget) |
| `POST` | `/api/v1/reservations` | 사전예약 저장 | `201 Created` + `{"id":"uuid"}` | `409 Conflict` (중복 이메일), `400` (유효성 실패) |
| `GET` | `/api/v1/admin/metrics` | 일별 퍼널 집계 | `200 OK` + JSON 배열 | `401 Unauthorized` (헤더 토큰 없음) |

### `POST /api/v1/visitors/bootstrap`

요청 본문 없음. 쿠키(`visitor_id`)가 이미 있으면 `last_seen_at` 갱신, 없으면 새 UUID 생성 후 INSERT.

```json
// Response 200
{ "visitorId": "uuid" }
```

### `POST /api/v1/events` 요청

```json
{
  "visitorId": "uuid",
  "sessionId": "uuid",
  "eventName": "page_view",
  "path": "/",
  "properties": {}
}
```

`eventName` 허용값: `page_view` | `reserve_opened` | `reservation_completed`

### `POST /api/v1/reservations` 요청

```json
{
  "sessionId": "uuid",
  "reservationType": "QUICK_AI_INTERVIEW",
  "name": "홍길동",
  "email": "hello@example.com",
  "privacyConsent": true,
  "utm": { "source": "instagram", "campaign": "waitlist" }
}
```

`reservationType` 허용값: `QUICK_AI_INTERVIEW` | `REVERSE_MATCHING`

### `GET /api/v1/admin/metrics` 응답

요청 헤더: `Authorization: Bearer <ADMIN_TOKEN>`

```json
[
  {
    "day": "2026-09-28",
    "uniqueVisitors": 120,
    "sessions": 145,
    "reservingVisitors": 18,
    "reservations": 18,
    "visitorConversionRate": 15.00
  }
]
```

## DB 스키마

스키마는 `supabase/migrations/202609280001_initial_landing_analytics.sql`로 관리한다. API는 migration을 직접 실행하지 않는다.

| 테이블 | PK | 주요 컬럼 | 제약 |
| --- | --- | --- | --- |
| `visitors` | `uuid` | `first_seen_at`, `last_seen_at`, `first_utm` | — |
| `landing_sessions` | `uuid` | `visitor_id`, `landing_path`, `referrer`, `utm` | FK → visitors |
| `tracking_events` | `uuid` (auto) | `visitor_id`, `session_id`, `event_name`, `path`, `properties` | `page_view`, `reserve_opened`, `reservation_completed`, `section_view`, `cta_clicked`; `page_view`는 세션+경로, `section_view`는 세션+섹션별 1회 |
| `reservations` | `uuid` (auto) | `email`, `normalized_email` (generated), `reservation_type`, `privacy_consent_at` | `normalized_email` unique |

**뷰**: `daily_landing_funnel` — 일별 방문자/세션/전환율 집계. `GET /api/v1/admin/metrics`가 이 뷰를 직접 조회한다.

**권한**: 브라우저 롤(`anon`, `authenticated`)은 모든 테이블에 접근 불가. Spring API만 서버사이드 DB 롤로 연결한다.

## 패키지 구조 및 구현 순서

```
com.weiver.landing/
  config/
    CorsConfig.java          # WEB_ALLOWED_ORIGIN 쿠키 CORS 허용
    AdminAuthFilter.java     # Authorization 헤더 토큰 검증
  visitor/
    VisitorController.java
    VisitorService.java
    VisitorRepository.java
  event/
    EventController.java
    EventService.java
    EventRepository.java
  reservation/
    ReservationController.java
    ReservationService.java
    ReservationRepository.java
  admin/
    AdminController.java     # daily_landing_funnel 뷰 조회
  common/
    GlobalExceptionHandler.java  # 409, 400, 401 통일 처리
```

### 구현 순서

1. **CORS 설정** — `WebMvcConfigurer`로 `WEB_ALLOWED_ORIGIN` 적용
2. **`POST /api/v1/visitors/bootstrap`** — UUID 생성, `visitors` INSERT, `HttpOnly` 쿠키 발급
3. **`POST /api/v1/events`** — `tracking_events` INSERT; DB 오류 시도 `200` 반환 (fire-and-forget)
4. **`POST /api/v1/reservations`** — `normalized_email` unique 위반 시 `409` 반환
5. **`GET /api/v1/admin/metrics`** — `AdminAuthFilter` 토큰 검증 후 `daily_landing_funnel` 뷰 조회
6. **`GlobalExceptionHandler`** — `DataIntegrityViolationException` → 409, `MethodArgumentNotValidException` → 400

## 환경 변수 및 배포 고려사항

| 변수 | 설명 | 비고 |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | Supabase Postgres 접속 URL (`sslmode=require` 필수) | `jdbc:postgresql://...` |
| `SPRING_DATASOURCE_USERNAME` | DB 사용자 | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Supabase DB 비밀번호 | 서버사이드 전용 |
| `WEB_ALLOWED_ORIGIN` | CORS 허용 오리진 | 개발시 `http://localhost:5173` |
| `ADMIN_TOKEN` | 관리자 API 인증 토큰 | 임의 문자열, 배포 환경 Secret으로 주입 |

**보안 원칙**
- 모든 환경 변수는 `.env` 파일이 아닌 배포 환경의 Secret으로 주입한다.
- Supabase service-role 키는 프론트엔드에 노출하지 않는다.
- DB 취약점: Supabase 표준 포트(5432)가 아닌 connection pooler(6543)를 사용하는 것을 권장한다.

**DB migration 절차**

```bash
supabase db push   # supabase/migrations/ 적용
```

API 서버는 migration을 실행하지 않는다. Flyway를 중복 도입하지 않는다.
