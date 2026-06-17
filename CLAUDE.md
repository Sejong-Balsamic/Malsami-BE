# Malsami-BE 프로젝트 규칙

세종말싸미(Sejong Malsami) 백엔드. Spring Boot 3.4.2 / Java 17 멀티모듈(Gradle).

## 모듈 구조

- `SM-Web` — 진입점(부트 JAR), Controller·Config·운영 리소스(`SM-Web/src/main/resources`)
- `SM-Application` — 애플리케이션 서비스(조합 로직)
- `SM-Domain-*` — 도메인별 모듈(Academic / AI / Auth / Member / Notice / Post)
- `SM-Common` — 공통 유틸·엔티티·예외·외부 라이브러리 의존

실행 가능한 부트 JAR은 `SM-Web`만 생성한다(`bootJar` 산출물명 `SM-Web.jar` 고정).

## 🔐 민감정보·설정 파일 규칙 (중요)

설정 파일은 **git 추적 여부에 따라 민감정보 포함 가능 여부가 다르다.** 반드시 구분한다.

### git에 추적되는 파일 — 민감정보 절대 금지
- `SM-Web/src/main/resources/application.yml`
  - `.gitignore`에서 `!SM-*/src/main/resources/application.yml`로 **명시적으로 추적**된다.
  - API 키·비밀번호·토큰·project-id·credentials 경로 등 **민감정보를 절대 넣지 않는다.**
  - 여기에는 프로파일 import 선언, JPA/서블릿/스웨거 같은 **비밀이 아닌 공통 구조**만 둔다.

### gitignore되는 파일 — 민감정보는 여기에만
- `application-prod.yml`, `application-dev.yml` (gitignore)
- `yeopjeon.yml`, `score.yml`, `exp.yml`, `admin.yml`, `post-tier.yml` (gitignore)
- `firebase-admin-sdk.json`, `sejong-malsami-embedding.json`, GCP credentials JSON 등 (`SM-*/src/main/resources/*.json` gitignore)
- **모든 민감정보(API 키, project-id, credentials, DB 비밀번호, GCP 서비스계정 등)는 `application-prod.yml` 또는 위 secret 파일에만 넣는다.**

### secret 주입 흐름 (CI/CD)
- 운영 설정 파일들은 GitHub Secrets에 저장되고, **배포 워크플로우의 빌드 단계에서 파일로 생성**된다.
  - `APPLICATION_PROD_YML` → `application-prod.yml`
  - `YEOPJEON_YML` / `SCORE_YML` / `EXP_YML` / `ADMIN_YML` / `POSTTIER_YML` → 각 yml
  - `SEJONG_MALSAMI_EMBEDDING_JSON` → `sejong-malsami-embedding.json`
  - `FIREBASE_CONFIG_JSON` → `firebase-admin-sdk.json`
  - `FIREBASE_MESSAGING_SW_JS` → `static/firebase-messaging-sw.js`
  - `VERTEX_CREDENTIALS_JSON` → GCP 서비스계정 credentials JSON (Vertex AI 임베딩 fallback용)
- **새 설정 키(특히 `@Value`/`@ConfigurationProperties`로 주입되는 키)를 추가하면, 반드시 다음 3곳을 함께 정합화한다.**
  1. `application-prod.yml`(로컬 secret)에 실제 값 추가
  2. `APPLICATION_PROD_YML` GitHub Secret 내용 업데이트(사용자 작업)
  3. 별도 파일이 필요하면(예: credentials JSON) GitHub Secret + 배포/CI 워크플로우의 파일 생성 단계 추가
- 설정 키만 코드에 추가하고 secret을 빠뜨리면 **prod 기동 시 placeholder 미해결로 앱이 죽는다.** (`@Value`는 기본값(`${key:}`)을 주어 방어 가능하나, 실제 값은 secret으로 채워야 기능이 동작한다)

## CI/CD 워크플로우 구조

- **main 브랜치 = CI 검증** — `MALSAMI-CI.yaml` (빌드 검증, `-x test`, 배포 없음)
- **deploy 브랜치 = 배포** — `PROJECT-SPRING-SIMPLE-CICD.yaml` (빌드 → DockerHub → Synology SSH → docker run → 헬스체크)
  - 배포하려면 `main`에서 `deploy` 브랜치를 갱신·push 한다.
- 버전 관리: `PROJECT-COMMON-VERSION-CONTROL`(main push 시 patch 자동 증가), `version.yml`이 기준.
- 무중단 배포(NGINX/TRAEFIK)·PR-PREVIEW·GitHub Packages publish·Secret 백업은 현재 **수동(workflow_dispatch) 전용**으로 비활성.
- 이슈 헬퍼는 `PROJECT-COMMON-SUH-ISSUE-HELPER-MODULE.yml` 하나만 사용(중복 제거됨).

## 빌드 메모

- 테스트 소스 일부가 패키지 리팩토링 미반영으로 CI에서 컴파일되지 않을 수 있어, CI/배포 빌드는 `-x test`로 수행한다.
- Gradle 의존성은 Maven Central 외에 사내 Nexus(`suh-project.synology.me:9999`)와 SUH Nexus(`nexus.suhsaechan.kr`)에서 받는다. `kr.suhsaechan:*` 라이브러리는 SUH Nexus 소속.
