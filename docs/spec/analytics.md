# 유입·사전예약 측정 기준

## 핵심 지표

| 지표 | 정의 | 용도 |
| --- | --- | --- |
| 방문 세션 | 브라우저 탭/세션별 landing 진입 | 캠페인 유입량 |
| 순 방문자 | `visitor_id` 쿠키 기준 중복 제거한 사람 | 사람 기준 모수 |
| 사전예약 완료 | 유효성 검증과 개인정보 동의를 통과해 저장된 예약 | 수요 |
| 방문자 전환율 | `사전예약 완료 순 방문자 / 순 방문자 * 100` | 기본 수요 전환율 |
| 세션 전환율 | `사전예약 완료 수 / 방문 세션 수 * 100` | 랜딩 효율 비교 |

대시보드의 기본 수치는 **방문자 전환율**로 표시합니다. 같은 쿠키에서 여러 번 예약해도 예약자는 1명으로 계산하며, 이메일은 소문자 정규화 후 중복을 막습니다.

## 식별 방식과 한계

- 첫 진입 시 API가 `visitor_id`(UUID)를 `HttpOnly`, `Secure`, `SameSite=Lax`, 180일 쿠키로 발급합니다.
- 프론트는 탭 단위 `session_id`를 `sessionStorage`에 보관하여 새 탭/브라우저 재시작의 세션을 구분합니다.
- 쿠키 삭제, 시크릿 모드, 다른 기기에서는 같은 사람을 합칠 수 없습니다. 따라서 이 지표는 로그인 사용자 수가 아니라 **쿠키 기반 추정 순 방문자**입니다.
- 분석/광고 쿠키 동의 정책이 필요한 지역·채널이라면 동의 후에만 비필수 이벤트를 전송합니다. 사전예약 제출에 필요한 최소 기록과 개인정보 처리방침은 별도로 검토합니다.

## 이벤트 흐름

```text
광고/공유 링크
  → GET /api/v1/visitors/bootstrap (visitor_id 쿠키 발급)
  → POST /api/v1/events { page_view, session_id, UTM, referrer }
  → POST /api/v1/reservations { type, name, email, consent }
  → Supabase Postgres
  → GET /api/v1/admin/metrics?from=...&to=...
```

`page_view`는 같은 `visitor_id + session_id + path` 조합에서 1회만 저장합니다. 이 규칙은 새로고침으로 유입을 부풀리지 않기 위함입니다.

## 예약 유형

- `QUICK_AI_INTERVIEW`: 간편 AI 면접 체험
- `REVERSE_MATCHING`: 역매칭 준비 체험

유형별 예약 수와 유형별 전환율도 동일한 방문자 모수로 제공해 어떤 데모 수요가 더 큰지 비교합니다.
