FROM openjdk:17-jdk-slim

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일을 복사
COPY build/libs/sejong-malsami-backend-0.0.1-SNAPSHOT.jar /app.jar

# 애플리케이션을 실행하는 명령 설정
ENTRYPOINT ["java", "-jar", "/app.jar"]

# Spring Boot 서버 포트 노출
EXPOSE 8080
