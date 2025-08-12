FROM gradle:8.10.2-jdk17 AS builder

# 작업 디렉토리 설정
WORKDIR /build

# 소스 복사
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle/
COPY cs25-service cs25-service/
COPY cs25-entity cs25-entity/
COPY cs25-common cs25-common/

# 테스트 생략하여 빌드 안정화
RUN ./gradlew :cs25-service:bootJar --stacktrace --no-daemon
FROM eclipse-temurin:17-jre-jammy

# 메타 정보
LABEL type="application" module="cs25-service"

# 작업 디렉토리
WORKDIR /apps

# Node.js + npm 설치 후, MCP 서버 전역 설치 + 심볼릭 링크 생성 + 빌드 타임 확인
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl ca-certificates gnupg bash \
 && curl -fsSL https://deb.nodesource.com/setup_20.x | bash - \
 && apt-get install -y --no-install-recommends nodejs \
 && npm install -g @modelcontextprotocol/server-brave-search \
 && ln -sf "$(npm bin -g)/server-brave-search" /usr/local/bin/server-brave-search \
 \
 # ===== 실행 가능 여부 확인 =====
 && echo "=== npm bin 경로 확인 ===" \
 && npm bin -g \
 && echo "=== server-brave-search 바이너리 확인 ===" \
 && ls -l "$(npm bin -g)/server-brave-search" \
 && ls -l /usr/local/bin/server-brave-search \
 && echo "=== server-brave-search --help 실행 ===" \
 && /usr/local/bin/server-brave-search --help || (echo "[ERROR] server-brave-search 실행 실패" && exit 1) \
 \
 && npm cache clean --force \
 && apt-get purge -y gnupg \
 && apt-get autoremove -y --purge \
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/*

# jar 복사
COPY --from=builder /build/cs25-service/build/libs/*.jar app.jar

# 포트 오픈
EXPOSE 8080

# 실행
ENTRYPOINT ["java", "-jar", "/apps/app.jar"]
