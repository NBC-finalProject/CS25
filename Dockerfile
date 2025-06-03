FROM openjdk:17
ARG CS25_JAR_FILE=*.jar
COPY ${CS25_JAR_FILE} cs25_app.jar
ENTRYPOINT ["java","-jar","/cs25_app.jar"]