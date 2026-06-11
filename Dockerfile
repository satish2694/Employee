### Multi-stage Dockerfile for production
FROM gradle:9-jdk17 as builder
WORKDIR /home/gradle/project
COPY --chown=gradle:gradle . /home/gradle/project
RUN gradle bootJar --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
ARG JAR_FILE=build/libs/*-boot.jar
COPY --from=builder /home/gradle/project/${JAR_FILE} /app/app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]

