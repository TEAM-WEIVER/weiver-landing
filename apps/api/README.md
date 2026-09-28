# API

Spring Boot API입니다. 책임은 다음으로 제한합니다.

- 방문자 쿠키 발급 및 세션/이벤트 기록
- 사전예약 검증 및 저장
- 운영용 집계 API 제공
- Supabase Postgres 접근 및 개인정보 보호

Java 21, Spring Boot 3.x, Gradle, Spring Web, Validation, PostgreSQL Driver, Actuator 기본 골격을 추가했습니다. 데이터 접근 계층과 엔드포인트는 `docs/api-contract.md`를 기준으로 구현합니다. Supabase migration은 Supabase CLI로 별도 적용하므로 API에 Flyway를 중복 도입하지 않습니다.

환경 변수는 `.env`가 아닌 배포 환경의 Secret으로 주입합니다.

```text
SPRING_DATASOURCE_URL=jdbc:postgresql://...:6543/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=<Supabase DB password>
WEB_ALLOWED_ORIGIN=https://example.com
```

Supabase service-role 키를 프론트엔드에 넣지 않습니다. DB 연결 정보 또는 서버 전용 service-role 키만 API 런타임에 둡니다.
