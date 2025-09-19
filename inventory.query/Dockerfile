FROM openjdk:17-jdk-slim
COPY target/*.jar inventory.query.jar
ENTRYPOINT ["java", "-jar", "/inventory.query.jar"]