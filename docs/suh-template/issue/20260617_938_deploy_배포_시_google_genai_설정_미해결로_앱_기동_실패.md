🗒️ 설명
---

deploy(prod) 배포 시 애플리케이션이 기동 도중 죽는 현상이 발생한다.

빌드·Docker 이미지 푸시·서버 컨테이너 실행까지는 정상이지만, Spring 컨텍스트 초기화 단계에서 `EmbeddingConfig` 빈 생성에 실패하면서 애플리케이션이 즉시 종료된다.

원인은 `EmbeddingConfig`가 요구하는 `google.genai.*` 프로퍼티가 운영 설정에서 해결되지 않기 때문이다.

```
Caused by: org.springframework.util.PlaceholderResolutionException:
  Could not resolve placeholder 'google.genai.project-id' in value "${google.genai.project-id}"
→ Error creating bean with name 'embeddingConfig'
→ Application run failed
```

`EmbeddingConfig`(`SM-Web/src/main/java/com/balsamic/sejongmalsami/web/config/EmbeddingConfig.java`)는 다음 6개 프로퍼티를 `@Value`로 주입받는다.

- `google.genai.project-id`
- `google.genai.location`
- `google.genai.credentials-file`
- `google.genai.use-vertex-ai`
- `google.genai.model`
- `google.genai.cloud-platform-url`

그러나 `application.yml`의 `spring.config.import`(`yeopjeon.yml, score.yml, exp.yml, admin.yml, post-tier.yml`) 어디에도 `google.genai` 설정이 포함되어 있지 않아, 운영 환경에서 해당 프로퍼티가 비어 있으면 컨텍스트 로딩이 실패한다.

> 현재 이 embedding / GenAI 기능은 더 이상 사용하지 않는다. 따라서 누락된 설정을 채우는 것보다, **관련 설정·빈 의존을 제거하거나 조건부로 비활성화**하는 방향이 적절하다.

🔄 재현 방법
---

1. deploy 브랜치에 push 하여 prod 프로파일로 배포한다.
2. 서버에서 컨테이너가 실행된 직후 로그를 확인한다.
3. `embeddingConfig` 빈 생성 실패와 함께 `Could not resolve placeholder 'google.genai.project-id'` 에러로 애플리케이션이 종료되는 것을 확인한다.

📸 참고 자료
---

```
2026-06-17 11-18-50 [main] WARN  ... relation "yeopjeon" does not exist, skipping
2026-06-17 11-18-50 [main] WARN  ... Exception encountered during context initialization - cancelling refresh attempt:
  org.springframework.beans.factory.BeanCreationException:
  Error creating bean with name 'embeddingConfig': Injection of autowired dependencies failed
2026-06-17 11-18-50 [main] INFO  com.zaxxer.hikari.HikariDataSource - HikariPool-1 - Shutdown initiated...
2026-06-17 11-18-50 [main] ERROR org.springframework.boot.SpringApplication - Application run failed
Caused by: org.springframework.util.PlaceholderResolutionException:
  Could not resolve placeholder 'google.genai.project-id' in value "${google.genai.project-id}"
```

- 관련 파일
  - `SM-Web/src/main/java/com/balsamic/sejongmalsami/web/config/EmbeddingConfig.java`
  - `SM-Domain-AI/src/main/java/com/balsamic/sejongmalsami/ai/service/OpenAIEmbeddingService.java`
  - `SM-Web/src/main/resources/application.yml` (`spring.config.import`)

✅ 예상 동작
---

- 사용하지 않는 embedding / GenAI 의존을 제거하거나, 프로파일·조건부 빈(`@ConditionalOnProperty` 등)으로 비활성화하여 해당 설정이 없어도 애플리케이션이 정상 기동해야 한다.
- prod 배포 시 컨텍스트 초기화가 실패 없이 완료되고, 컨테이너가 정상적으로 유지되어야 한다.

⚙️ 환경 정보
---

- **OS**: Synology NAS (배포 서버), Ubuntu(GitHub Actions Runner)
- **런타임**: Java 17 (eclipse-temurin:17-jdk), Spring Boot 3.4.2, prod 프로파일
- **배포 경로**: deploy 브랜치 push → PROJECT-SPRING-SIMPLE-CICD (Docker + SSH)

🙋‍♂️ 담당자
---

- **백엔드**: Cassiiopeia
- **프론트엔드**:
- **디자인**:
