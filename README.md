# weiver landing page

weiver 데모 버전 테스터 모집을 위한 랜딩페이지입니다. 현재 루트의 메인 페이지는 hero-scroll 인터랙션 버전이며, 단일 레포에서 React 프론트엔드, Spring API, Supabase(Postgres) 스키마를 함께 관리합니다.

## Live

https://weiver-landing.vercel.app

## Repository layout

```text
apps/
  web/                 # React + Vite 랜딩 (신규 구현 위치)
  api/                 # Spring Boot API
supabase/migrations/   # Supabase Postgres 마이그레이션
docs/                  # 지표 정의와 API 계약
assets/                # 현재 hero-scroll 랜딩 에셋
archive/               # 이전 랜딩 소스 보관
index.html             # 현재 배포되는 hero-scroll 랜딩
```

## Funnel metric

기본 수요 지표는 `사전예약 완료 순 방문자 ÷ 쿠키 기반 순 방문자 × 100`입니다. 세션 수 기준 보조 지표도 함께 기록합니다. 상세한 기준과 한계는 [analytics.md](docs/analytics.md)를 참고하세요.

## Preview locally

현재 랜딩은 별도 설치 없이 `index.html`을 브라우저에서 열어 확인할 수 있습니다. CSS 마스크 로고가 있으므로 간단한 로컬 서버로 여는 것을 권장합니다. React 전환 이후에는 `npm install && npm run dev:web`를 사용합니다.

## Notes

- Pretendard 및 weiver 디자인 시스템 적용
- 데스크톱·모바일 반응형 지원
- 두 가지 데모 체험 방식 선택 가능
- 현재 hero-scroll 랜딩은 인터랙션 중심의 정적 화면이며, 사전예약 수집 API는 아직 연결되지 않았습니다.
- 저장/집계 구현의 기준 스키마는 `supabase/migrations`에, API 통신 계약은 `docs/api-contract.md`에 있습니다.
