# Landing React 전환 계획

2026-09-28

현재 `index.html` 단일 파일(3122줄)을 `apps/web` React 앱으로 단계적으로 이전하고, Spring API를 연결한다.

## 전환 목표와 완료 기준

현재 `index.html`은 API가 연결되지 않은 정적 파일이다. React로 전환하면 컴포넌트 분리와 상태 관리가 가능해지고, 방문자/이벤트/예약 API를 연결할 수 있다.

**완료 기준**
- 현재 `index.html`의 모든 시각적 인터랙션(히어로 스크롤, 씬 전환, 프로덕트 스토리)이 React에서 동일하게 동작한다
- `POST /api/v1/visitors/bootstrap`, `POST /api/v1/events`, `POST /api/v1/reservations` 세 엔드포인트가 연결된다
- Vercel에 `apps/web` 빌드가 배포된다
- 루트 `index.html`은 `archive/`로 이동된다

## 단계별 계획

각 페이즈는 독립적으로 배포/확인 가능한 상태로 끝난다. 검토 게이트를 통과한 후 다음 페이즈를 시작한다.

### Phase 1 — 환경 기반 구성

범위: `apps/web` 프로젝트 세팅, CSS 토큰 이전, 레이아웃 골격

**산출물**
- Pretendard/IBM Plex Sans 폰트, `--teal`/`--blue` 등 CSS 시스템 토큰 이전
- `SiteHeader` 컴포넌트 (로고 + 스크롤 `solid` 상태)
- 페이지 러너(`App.jsx`) 바닥 구조

**검토 게이트**: 헤더가 화면에 뜬다, 스크롤 시 solid 바닥이 동작한다

---

### Phase 2 — Hero 인터랙션 이전

범위: canvas terrain, 씬 시스템, 히어로 스크롤텔링(`--p`) 로직

**산출물**
- `HeroStage` 컴포넌트 (스크롤 주사위, `--p` CSS 변수)
- `TerrainCanvas` (현 canvas 코드 이전)
- `ScenePlayer` (씬 자동 전환 + 일시정지)

**검토 게이트**: 히어로 스크롤 시 `--p` 변화, terrain 애니메이션, 씬 전환이 기존과 동일하게 동작한다

---

### Phase 3 — 씬 컴포넌트 이전

범위: s1~s6까지 7개 씬을 카드 컴포넌트로 분리

**산출물**
- `scenes/Scene1` ~ `Scene6b` (ScenePlayer에 주입)
- `RadarChart` SVG 컴포넌트 (s4 역량 프로필)
- `DashboardFrame` 컴포넌트 (s4b 대시보드)

**검토 게이트**: 모든 씬이 기존 디자인대로 렌더링된다, 모바일 렌더링도 확인

---

### Phase 4 — Content 섹션 이전

범위: hero 이후 본문 영역 (프로덕트 스토리, 가치 제안, 예약 폼 하단)

**산출물**
- `ProductStory` (스크롤 줌 지실)
- `ValueSection`, `ReservationSection` 컴포넌트
- 예약 폼 UI (올서 스타일링, 메세지 표시 없음)

**검토 게이트**: 전체 페이지가 기존 `index.html`과 시각적으로 동등하다, Vercel 선보리로 확인 가능

---

### Phase 5 — API 연결

범위: `visitors/bootstrap`, `events`, `reservations` 엔드포인트 연결

**산출물**
- `api/` 클라이언트 모듈 (fetch 래퍼)
- 쿠키 및 `sessionStorage` 방문자 식별 로직
- 예약 폼 제출 실제 연결, 중복 409 처리

**검토 게이트**: 실제 예약이 Supabase에 저장된다, 이벤트 로그가 쌓인다

---

### Phase 6 — 콘텐츠 대체 및 배포

범위: Vercel에서 `apps/web` 빌드를 서빙하도록 전환, 루트 `index.html` 아카이브

**산출물**
- `vercel.json` 빌드 루트를 `apps/web`으로 변경
- 루트 `index.html` → `archive/`
- 실사용 URL 확인

**검토 게이트**: `weiver-landing.vercel.app`에서 React 앱이 서빙된다

## 컴포넌트 구조

```
apps/web/src/
  api/
    client.js          # fetch 래퍼 (베이스 URL, 에러 핸들링)
    visitors.js        # bootstrap 호출
    events.js          # 이벤트 전송
    reservations.js    # 예약 제출
  components/
    SiteHeader.jsx     # 로고 + 스크롤 solid 상태
    hero/
      HeroStage.jsx    # sticky 컨테이너, --p 주사위
      TerrainCanvas.jsx
      ScenePlayer.jsx  # 씬 시퀀서 + 일시정지
      scenes/
        Scene1.jsx ~ Scene6b.jsx
        RadarChart.jsx
        DashboardFrame.jsx
    content/
      ProductStory.jsx  # 스크롤 줌 지실
      ValueSection.jsx
      ReservationSection.jsx  # 엔드포인트 연결 폼
  styles/
    tokens.css         # :root CSS 시스템 토큰
    base.css           # reset, typography
    components.css     # 버튼, 타입용 공통 클래스
  App.jsx
  main.jsx
```

`api/` 모듈은 Phase 5에 구현, 각 사이드 이펙트는 Phase 2~4에 채워나간다. CSS는 CSS Modules이나 Tailwind 없이 토큰 + BEM 방식으로 유지한다(현재 앱과 동일한 접근방식).

## API 연결 전략

| 엔드포인트 | 연결 시점 | 실패 처리 |
| --- | --- | --- |
| `POST /api/v1/visitors/bootstrap` | Phase 5 — 앱 마운트 시 1회 호출, `visitor_id`는 HttpOnly 쿠키로 관리 | 실패해도 UI 차단 없음 |
| `POST /api/v1/events` | Phase 5 — `page_view`는 마운트 후, `reserve_opened`는 폼 진입 시 | fire-and-forget, 오류 무시 |
| `POST /api/v1/reservations` | Phase 5 — 폼 submit 핸들러 | 409 → "이미 신청하셨습니다" 메시지, 400 → 필드 오류 표시 |

`session_id`는 `sessionStorage`에 UUID를 직접 생성해 보관한다. API 서버가 없는 Phase 1~4 개발 중에는 `api/` 모듈을 no-op stub으로 대체하여 UI 개발과 분리한다.

## 리스크와 결정 필요 항목

**결정 필요**

1. **CSS 방식**: 토큰 + BEM(현재 방식 유지) vs CSS Modules 도입. 컴포넌트가 늘어나면 Modules이 추후되지만 전환 비용이 있음. 단기에는 BEM으로 시작하는 것을 권장.
2. **API 베이스 URL**: `VITE_API_BASE_URL` 환경 변수로 관리. 개발 중 stub / 프로덕션 API 서버 주소를 언제 확정할지 Phase 5 시작 전에 결정 필요.
3. **Phase 6 배포 전환 시점**: Phase 4 후 Vercel 선배포로 선제배포 가능. 루트 `index.html` 아카이브 시점을 Phase 5 완료 후로 할지 Phase 4 후로 할지 결정.

**리스크**

- `DashboardFrame`(s4b)은 스케일 계산 로직이 복잡해 Phase 3에서 가장 시간이 많이 소요될 수 있음.
- canvas terrain을 React에서 `useEffect`로 연결할 때 `requestAnimationFrame` 정리가 누락되면 메모리 리크 발생. cleanup 로직 주의 필요.
