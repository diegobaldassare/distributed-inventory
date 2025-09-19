FROM openjdk:17-jdk-slim
COPY target/*.jar inventory.cmd.jar
ENTRYPOINT ["java", "-jar", "/inventory.cmd.jar"]