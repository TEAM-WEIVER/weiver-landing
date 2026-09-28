# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository overview

weiver 데모 테스터 모집 랜딩페이지. 단일 레포에서 React 프론트엔드(`apps/web`), Spring Boot API(`apps/api`), Supabase 마이그레이션(`supabase/migrations`)을 함께 관리한다.

루트의 `index.html`은 현재 배포 중인 정적 hero-scroll 랜딩(별도 빌드 없음). `apps/web`은 React 전환 준비 중인 신규 구현 위치다.

`docs/`는 유형별로 구성된다: `planning/` (기획·마이그레이션 계획), `spec/` (API 계약·분석 지표 정의), `assets/` (리소스 교체 가이드).

## Commands

### Frontend (`apps/web`)

```bash
npm run dev:web        # 개발 서버 (루트에서 실행)
npm run build:web      # 프로덕션 빌드 (루트에서 실행)

cd apps/web && npm run dev      # 직접 실행도 가능
```

Node.js 20 이상 필요.

### API (`apps/api`)

```bash
cd apps/api
./gradlew bootRun                          # 서버 실행
./gradlew test                             # 전체 테스트
./gradlew test --tests "클래스명.메서드명"  # 단일 테스트
./gradlew build                            # 빌드 + 테스트
./gradlew build -x test                    # 테스트 제외 빌드
```

Java 21 필요. 환경 변수 없이 실행하면 DB 연결 실패로 기동 불가 — 로컬 테스트 시 `application-local.yml`을 별도로 구성하거나 테스트 슬라이스(`@WebMvcTest` 등)로 격리한다.

### Supabase migration

```bash
supabase db push    # supabase/migrations/ 적용
```

API 서버는 마이그레이션을 실행하지 않는다(Flyway 미사용).

## Architecture

### 전체 흐름

```
브라우저
  → POST /api/v1/visitors/bootstrap  (visitor_id HttpOnly 쿠키 발급)
  → POST /api/v1/events              (page_view 등 익명 이벤트)
  → POST /api/v1/reservations        (사전예약)
  → GET  /api/v1/admin/metrics       (관리자 전용 집계)
```

프론트는 탭 단위 `session_id`를 `sessionStorage`에, 방문자 식별은 `visitor_id` 쿠키(180일)로 관리한다.

### API 설계 원칙

- **이벤트 기록 실패는 랜딩 사용을 차단하지 않는다** — `/api/v1/events`는 DB 오류가 나도 `200`을 반환한다(fire-and-forget).
- **이메일 중복은 `409`** — `normalized_email`(소문자 trim, DB generated column) unique 제약을 DataIntegrityViolationException으로 잡아 처리한다.
- **브라우저는 DB에 직접 접근하지 않는다** — Supabase service-role 키는 API 런타임에만 존재하고, `anon`/`authenticated` 롤에는 모든 테이블 권한이 없다.
- **관리자 인증은 Bearer 토큰** — `ADMIN_TOKEN` 환경 변수와 요청 헤더 비교, `OncePerRequestFilter`로 구현.

### 데이터 계층

ORM 없이 Spring 6.1의 `JdbcClient`를 사용한다. 스키마는 `supabase/migrations`가 단일 소스이며 Entity 클래스와 충돌이 없다.

DB 테이블 4개: `visitors` → `landing_sessions` → `tracking_events`, `reservations`. `daily_landing_funnel` 뷰가 admin metrics의 기반이다.

핵심 지표: **방문자 전환율** = `사전예약 완료 순 방문자 ÷ 쿠키 기반 순 방문자 × 100`

### 환경 변수

| 변수 | 설명 |
| --- | --- |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://...:6543/postgres?sslmode=require` (pooler 포트) |
| `SPRING_DATASOURCE_USERNAME` | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Supabase DB 비밀번호 |
| `WEB_ALLOWED_ORIGIN` | CORS 허용 오리진 (기본값 `http://localhost:5173`) |
| `ADMIN_TOKEN` | 관리자 API 인증 토큰 |

모두 배포 환경의 Secret으로 주입한다. `.env` 파일을 커밋하지 않는다.

## Workflow

기능 개발은 다음 순서로 진행한다.

1. **기능 브리핑** — 목적·범위·수용 기준 정리 (Hermes로 공유)
2. **기획** — User Story + Acceptance Criteria 작성
3. **테스트 코드 작성** — AC 기반으로 먼저 작성 (Red)
4. **구현** — 테스트를 통과시키는 최소 코드 (Green)

브랜치 전략: `feat/<기능명>` → PR → `main`.
