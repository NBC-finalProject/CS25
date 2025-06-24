# ./k6/Dockerfile-k6
#FROM golang:1.24 as builder
#
#WORKDIR /src
#RUN git clone https://github.com/grafana/xk6.git && \
#    cd xk6 && \
#    go install ./cmd/xk6
#
#ENV PATH="/go/bin:$PATH"
#
#RUN xk6 build \
#  --with github.com/grafana/xk6-output-prometheus-remote
#
#FROM alpine:3.18
#COPY --from=builder /src/k6 /usr/bin/k6
#ENTRYPOINT ["k6"]


# Step 1: Go 환경에서 k6 + Prometheus 확장 빌드
FROM golang:1.24 as builder

RUN go install go.k6.io/xk6/cmd/xk6@latest
RUN xk6 build --output /k6 \
  --with github.com/grafana/xk6-output-prometheus-remote@latest

# Step 2: 기본 k6 이미지에 빌드된 k6 복사
FROM grafana/k6:latest
COPY --from=builder /k6 /usr/bin/k6
