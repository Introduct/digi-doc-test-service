FROM openjdk:11.0.5-jdk

ARG JAR_FILE
COPY ${JAR_FILE} app.jar

ENTRYPOINT [ "java", "-jar", "/app.jar" ]