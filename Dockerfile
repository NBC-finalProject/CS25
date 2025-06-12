# 멀티 스테이지 빌드: Gradle 빌더
FROM gradle:8.10.2-jdk17 AS builder

# 작업 디렉토리 설정
WORKDIR /apps

# 소스 복사
COPY . /apps

# 테스트 생략하여 Docker 빌드 안정화
RUN gradle clean build -x test --no-daemon

# 실행용 경량 이미지
FROM openjdk:17

# 메타 정보
LABEL type="application"

# 앱 실행 디렉토리
WORKDIR /apps

# jar 복사 (빌더 스테이지에서)
COPY --from=builder /apps/build/libs/*.jar /apps/app.jar

# 포트 오픈
EXPOSE 8080

# 앱 실행 명령
ENTRYPOINT ["java", "-jar", "/apps/app.jar"]