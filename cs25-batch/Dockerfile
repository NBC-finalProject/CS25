FROM gradle:8.10.2-jdk17 AS builder

# 작업 디렉토리 설정
WORKDIR /apps

# 전체 프로젝트 복사 (멀티모듈 의존성 포함)
COPY .. .

# cs25-batch 모듈만 빌드하여 bootJar 생성
RUN gradle :cs25-batch:clean :cs25-batch:bootJar -x test

# jdk 대신 용량이 작은 jre 사용
FROM eclipse-temurin:17-jre

# 메타 정보
LABEL type="application" module="cs25-batch"

# 작업 디렉토리
WORKDIR /apps

# bootJar 복사 (Spring Boot executable jar)
COPY --from=builder /apps/cs25-batch/build/libs/cs25-batch-0.0.1-SNAPSHOT.jar app.jar

# 포트 오픈 (service는 8080)
EXPOSE 8081

# 실행 (Spring Boot의 표준 방식)
ENTRYPOINT ["java", "-jar", "/apps/app.jar"]