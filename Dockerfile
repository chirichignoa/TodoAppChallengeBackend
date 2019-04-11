FROM java:8

WORKDIR /app
COPY /target/backend-0.0.1-SNAPSHOT.jar /app
ENTRYPOINT ["java", "-jar", "backend-0.0.1-SNAPSHOT.jar"]

EXPOSE 8080