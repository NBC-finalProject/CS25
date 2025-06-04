FROM openjdk:17
COPY build/libs/cs25-app-0.0.1-SNAPSHOT.jar /cs25_app.jar
ENTRYPOINT ["java", "-jar", "/cs25_app.jar"]
