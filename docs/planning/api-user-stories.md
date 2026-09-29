# API User Stories & Acceptance Criteria

2026-09-28

F1~F4 + 공통 관심사 기반. 각 AC는 테스트 코드 작성의 직접 입력으로 사용한다.

스키마 단일 소스: `supabase/migrations/202609280001_initial_landing_analytics.sql`

---

## F1 — 방문자 부트스트랩

> **US-1** 랜딩 페이지를 처음 방문한 사용자로서,  
> 내 방문을 식별하는 쿠키를 발급받아  
> 이후 행동이 같은 방문자로 묶일 수 있다.

**요청 바디:**
```json
{
  "sessionId": "<탭 단위 UUID, 프론트 sessionStorage에서 생성>",
  "path": "/",
  "referrer": "<optional>",
  "utm": { "source": "google", "medium": "cpc", "campaign": null, "content": null, "term": null }
}
```

### AC-1-1 신규 방문자 쿠키 발급
- `POST /api/v1/visitors/bootstrap` 요청 시 `visitor_id` 쿠키가 없으면
- 새 UUID를 생성해 `visitors` 테이블에 INSERT
- 요청 바디의 `sessionId`, `path`, `referrer`, `utm`으로 `landing_sessions`에 INSERT
- 응답에 `Set-Cookie: visitor_id=<uuid>; HttpOnly; SameSite=Lax; Max-Age=15552000` 포함
- 응답 바디 `{ "visitorId": "<uuid>" }`, 상태 `200`

### AC-1-2 재방문자 갱신
- 유효한 `visitor_id` 쿠키와 함께 요청하면
- `visitors.last_seen_at` UPDATE (신규 INSERT 없음)
- 요청 바디의 `sessionId`가 `landing_sessions`에 없으면 INSERT, 이미 존재하면 무시
- 응답 쿠키의 UUID는 기존 값과 동일
- 응답 바디 `{ "visitorId": "<기존 uuid>" }`, 상태 `200`

### AC-1-3 존재하지 않는 쿠키 값 처리
- 쿠키에 UUID가 있지만 `visitors` 테이블에 해당 레코드가 없으면
- 새 UUID를 생성해 `visitors`에 INSERT (silent recovery, 새 UUID로 쿠키 교체)
- 요청 바디의 `sessionId`로 `landing_sessions`에 INSERT
- 응답에 새 `Set-Cookie` 포함, 응답 바디 `{ "visitorId": "<새 uuid>" }`, 상태 `200`

---

## F2 — 이벤트 트래킹

> **US-2** 랜딩 페이지를 탐색하는 사용자로서,  
> 내 행동(페이지 뷰, 폼 열기, 예약 완료)이 기록되어  
> 서비스 운영팀이 전환 퍼널을 분석할 수 있다.

**요청 바디:**
```json
{
  "visitorId": "<UUID>",
  "sessionId": "<UUID>",
  "eventName": "page_view",
  "path": "/"
}
```

### AC-2-1 정상 이벤트 저장
- 요청 바디에 `visitorId`(UUID 형식), `sessionId`(UUID 형식), `eventName`(`page_view`|`reserve_opened`|`reservation_completed`), `path` 포함 시
- `tracking_events` 테이블에 INSERT
- 상태 `200` 반환
- `visitorId`·`sessionId`의 DB 존재 여부는 검증하지 않는다 (fire-and-forget 원칙 유지)

### AC-2-2 fire-and-forget — DB 오류 무시
- DB INSERT 실패(네트워크 오류, 제약 위반 등)가 발생해도
- 예외를 catch하여 **상태 `200` 반환** (랜딩 흐름 차단 금지)
- 오류 내용은 서버 로그에만 기록

### AC-2-3 허용되지 않는 eventName 거부
- `eventName`이 세 허용값 외의 값이면 상태 `400` 반환

### AC-2-4 필수 필드 누락 거부
- `visitorId`, `sessionId`, `eventName`, `path` 중 하나라도 누락 시 `400` 반환

---

## F3 — 사전예약

> **US-3** 데모 테스터로 참여하려는 사용자로서,  
> 이름과 이메일로 사전예약을 제출하여  
> 서비스 출시 시 연락을 받을 수 있다.

**요청 바디:**
```json
{
  "visitorId": "<UUID>",
  "sessionId": "<UUID>",
  "name": "홍길동",
  "email": "test@example.com",
  "reservationType": "QUICK_AI_INTERVIEW",
  "privacyConsentAt": "2026-09-28T12:00:00Z",
  "utm": { "source": null, "medium": null, "campaign": null, "content": null, "term": null }
}
```

### AC-3-1 정상 예약 저장
- 유효한 `visitorId`, `sessionId`, `name`, `email`, `reservationType`, `privacyConsentAt` 포함 요청 시
- `reservations` 테이블에 INSERT
- 상태 `201 Created`, 응답 바디 `{ "id": "<uuid>" }`

### AC-3-2 이메일 중복 거부
- 이미 예약된 이메일(`normalized_email` unique 제약)로 재요청 시
- **상태 `409 Conflict`** 반환, 바디 `{ "error": "DUPLICATE_EMAIL" }`
- 대소문자·앞뒤 공백 정규화는 DB generated column이 처리 (API는 raw 값 그대로 전달)

### AC-3-3 privacyConsentAt 누락 거부
- `privacyConsentAt` 필드가 없거나 null이면 `400 Bad Request`

### AC-3-4 이메일 형식 검증
- 이메일 형식이 유효하지 않으면 `400 Bad Request`

### AC-3-5 허용되지 않는 reservationType 거부
- `QUICK_AI_INTERVIEW`, `REVERSE_MATCHING` 외 값이면 `400 Bad Request`

### AC-3-6 utm 필드 선택적 허용
- `utm` 객체 전체 또는 개별 필드(`source`, `medium`, `campaign`, `content`, `term`) optional
- 누락 시 DB에 `'{}'::jsonb` 저장

---

## F4 — 어드민 지표

> **US-4** 서비스 운영자로서,  
> 일별 방문자·세션·예약 수와 전환율을 조회하여  
> 랜딩 페이지 성과를 모니터링한다.

### AC-4-1 인증 성공 시 지표 반환
- 헤더 `Authorization: Bearer <ADMIN_TOKEN>` 값이 환경 변수와 일치하면
- `daily_landing_funnel` 뷰 조회 결과를 날짜 내림차순 JSON 배열로 반환
- 상태 `200 OK`

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

### AC-4-2 인증 실패 → 401
- `Authorization` 헤더 미포함, 또는 헤더 값이 `ADMIN_TOKEN` 환경 변수와 다르면
- `401 Unauthorized` 반환

### AC-4-3 다른 엔드포인트는 필터 미적용
- `/api/v1/visitors/**`, `/api/v1/events`, `/api/v1/reservations` 는 `AdminAuthFilter` 통과하지 않음

---

## 공통 — CORS

### AC-C1 허용 오리진 요청 통과
- `Origin: <WEB_ALLOWED_ORIGIN>` 요청(단순 요청 및 preflight OPTIONS)에
- `Access-Control-Allow-Origin`, `Access-Control-Allow-Credentials: true` 응답 헤더 포함
- preflight는 `200 OK` 반환 (Spring MVC 기본값)

### AC-C2 미허용 오리진 차단
- 허용 오리진 외 `Origin` 헤더 요청에 CORS 에러 응답

---

## 공통 — 에러 응답 형식

### AC-E1 에러 응답 공통 포맷
- `4xx` 응답 바디는 항상 `{ "error": "<ERROR_CODE>" }` 형식
- `400`: Bean Validation 실패 시 `{ "error": "INVALID_REQUEST" }`
- `401`: 인증 실패 시 `{ "error": "UNAUTHORIZED" }`
- `409`: 이메일 중복 시 `{ "error": "DUPLICATE_EMAIL" }`
- `500`: 예상치 못한 예외 시 `{ "error": "INTERNAL_ERROR" }`

---

## 구현 순서 (AC 기준)

| 순서 | 대상 | AC |
|---|---|---|
| 1 | DB 마이그레이션 | AC-M1 (이미 완료) |
| 2 | CORS 설정 | AC-C1, AC-C2 |
| 3 | F1 방문자 부트스트랩 | AC-1-1 ~ 1-3 |
| 4 | GlobalExceptionHandler | AC-E1 |
| 5 | F2 이벤트 트래킹 | AC-2-1 ~ 2-4 |
| 6 | F3 사전예약 | AC-3-1 ~ 3-6 |
| 7 | F4 어드민 지표 + AdminAuthFilter | AC-4-1 ~ 4-3 |
