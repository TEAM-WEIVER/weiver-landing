# Web

React + Vite 기반 랜딩페이지입니다. 기존 루트의 정적 랜딩은 디자인 기준 원본으로 남겨 두었고, 실제 전환 작업은 이 디렉터리에서 진행합니다.

```bash
npm install
npm run dev:web
```

브라우저는 Supabase에 직접 쓰지 않습니다. 방문 및 사전예약 요청은 항상 Spring API로 보내며, API가 Supabase Postgres에 저장합니다.
