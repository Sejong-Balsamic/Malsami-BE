### 문제 요약
Docker 이미지 빌드 시 베이스 이미지(`openjdk:17-jdk-slim`) 조회 불가("not found")로 빌드 실패 | **타입**: CI/CD (Docker) | **환경**: GitHub Actions (Docker Buildx)

### 원인 분석
**근본 원인**: `Dockerfile`의 베이스 이미지로 정의된 `openjdk:17-jdk-slim`은 현재 Docker Hub 상에서 공식 유지보수가 중단(deprecated)되었으며, 보관 해제 또는 보존 만료 등으로 인해 해당 이미지 및 태그 조회가 불가능(`not found`)해졌습니다.

**발생 메커니즘**:
1. GitHub Actions 빌드 흐름 중 `Build and push Docker image` 단계에서 Docker buildx가 구동되었습니다.
2. `Dockerfile` 첫 줄인 `FROM openjdk:17-jdk-slim`을 읽어 Docker Hub에서 해당 베이스 이미지를 풀(pull)하려고 시도했습니다.
3. 이미지 소스 메타데이터 조회가 실패하며 `failed to resolve source metadata for docker.io/library/openjdk:17-jdk-slim: not found` 에러가 발생하여 빌드가 중단되었습니다.

### 해결 방법
#### Root Fix (권장)
사용이 만료된 `openjdk` 공식 이미지 대신, 현재 자바 및 스프링 생태계에서 장기 지원(LTS) 및 안정성이 공식 검증된 **`eclipse-temurin:17-jdk`** 베이스 이미지로 교체합니다. 
기존 데비안 슬림 기반의 패키지 호환성을 그대로 안전하게 유지하면서도 Docker Hub로부터 안정적으로 이미지를 받아와 빌드를 진행할 수 있습니다.

### 검증
1. `Dockerfile` 첫 줄 수정 반영 후 `main` 브랜치에 다이렉트 push하여 GitHub Actions 빌드 통과 여부 확인

### 재발 방지
- 향후 신규 프로젝트 작성 시 `openjdk` 대신 `eclipse-temurin` 또는 `amazoncorretto` 같은 LTS 보장 베이스 이미지 규격 사용 권장
