FROM openjdk:11.0.5-jdk as builder
WORKDIR /app
COPY mvnw mvnw
COPY .mvn .mvn
COPY pom.xml pom.xml

RUN ./mvnw -B dependency:go-offline

COPY src src

RUN ./mvnw -B clean package -DskipTestsdoc -Dmaven.test.skip=true

FROM openjdk:11.0.5-jdk
WORKDIR /app
COPY --from=builder /app/target/digi-doc-test-service.jar /app/digi-doc-test-service.jar
ENTRYPOINT ["java","-jar","/app/digi-doc-test-service.jar"]