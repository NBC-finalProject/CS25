FROM gradle:8.10.2-jdk17 AS builder

# 작업 디렉토리 설정
WORKDIR /apps

# 소스 복사 (모듈 전체가 아닌 현재 모듈만 복사)
COPY . .

# 테스트 생략하여 빌드 안정화
RUN gradle clean build -x test

FROM openjdk:17

# 메타 정보
LABEL type="application" module="cs25-batch"

# 작업 디렉토리
WORKDIR /apps

# jar 복사
COPY --from=builder /apps/cs25-batch/build/libs/cs25-batch-0.0.1-SNAPSHOT.jar app.jar

# 포트 오픈 (service는 8080)
EXPOSE 8081

# 실행
ENTRYPOINT ["java", "-jar", "/apps/app.jar"]
