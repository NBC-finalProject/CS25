# 멀티 스테이지 빌드
FROM gradle:8.10.2-jdk17 AS builder

# 작업 디렉토리 설정
WORKDIR /apps

# 빌더 이미지에서 애플리케이션 빌드
COPY . /apps
RUN gradle clean bootJar --no-daemon

# OpenJDK 17 slim 기반 이미지 사용
FROM openjdk:17

# 이미지에 레이블 추가
LABEL type="application"

# 작업 디렉토리 설정
WORKDIR /apps

# 애플리케이션 jar 파일을 컨테이너로 복사
#COPY build/libs/*.jar /apps/app.jar
COPY --from=builder /apps/build/libs/*-SNAPSHOT.jar /apps/app.jar

# 애플리케이션이 사용할 포트 노출
EXPOSE 8080

# 애플리케이션을 실행하기 위한 엔트리포인트 정의
ENTRYPOINT ["java", "-jar", "/apps/app.jar"]