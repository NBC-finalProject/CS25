FROM ubuntu:latest
LABEL authors="user"

ENTRYPOINT ["top", "-b"]