# -------- builder --------
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app

# Gradle 프로젝트가 backend/runnity/에 있으므로 그 폴더만 복사
COPY runnity/ ./
# 필요하다면 Gradle 캐시가 잘 활용되도록 settings.gradle*, build.gradle* 먼저 복사 → 소스 나중 복사로 최적화 가능
# 여기서는 심플하게 전체 복사로 진행

RUN gradle build -x test

# -------- runtime --------
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
