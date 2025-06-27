FROM gradle:8.10.2-jdk17 AS builder

# 작업 디렉토리 설정
WORKDIR /build

# 소스 복사 (모듈 전체가 아닌 현재 모듈만 복사)
COPY . .

# 실행 권한 부여
RUN chmod +x gradlew

# 테스트 생략하여 빌드 안정화
RUN ./gradlew :cs25-service:bootJar --no-daemon

# 실제 실행 이미지
FROM openjdk:17

# 메타 정보
LABEL type="application" module="cs25-service"

# 작업 디렉토리
WORKDIR /apps

# jar 복사
COPY --from=builder /build/cs25-service/build/libs/*.jar app.jar

# 포트 오픈 (service는 8080)
EXPOSE 8080

# 실행
ENTRYPOINT ["java", "-jar", "/apps/app.jar"]
