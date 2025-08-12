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

# Node.js 22 설치 + 공식 Brave MCP 서버 설치 + 래퍼 스크립트 생성
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl ca-certificates gnupg bash \
 && curl -fsSL https://deb.nodesource.com/setup_22.x | bash - \
 && apt-get install -y --no-install-recommends nodejs \
 \
 # 공식 패키지 설치 (deprecated 패키지 제거)
 && npm install -g @brave/brave-search-mcp-server \
 \
 # 전역 모듈 경로 계산
 && NPM_PREFIX="$(npm prefix -g)" \
 && SRCDIR="${NPM_PREFIX}/lib/node_modules/@brave/brave-search-mcp-server" \
 \
# 실행 래퍼 (args는 전부 "$@"로 위임)
&& { \
  echo '#!/bin/sh'; \
  echo 'NODE=$(command -v node || echo /usr/bin/node)'; \
  echo 'exec "$NODE" "'"$SRCDIR"'/dist/index.js" "$@"'; \
} > /usr/local/bin/server-brave-search \
&& chmod 0755 /usr/local/bin/server-brave-search \
 \
 # 설치/실행 점검
 && echo "=== which server-brave-search ===" && which server-brave-search \
 && echo "=== server-brave-search --help ===" && server-brave-search --help || (echo "[ERROR] server-brave-search 실행 실패" && exit 1) \
 \
 # 정리
 && npm cache clean --force \
 && apt-get autoremove -y --purge \
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/*


# jar 복사
COPY --from=builder /build/cs25-service/build/libs/*.jar app.jar

# 포트 오픈
EXPOSE 8080

# 실행
ENTRYPOINT ["java", "-jar", "/apps/app.jar"]
