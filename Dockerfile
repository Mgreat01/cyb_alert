FROM eclipse-temurin:25-jre

WORKDIR /app
RUN groupadd --system cyberwatch && useradd --system --gid cyberwatch cyberwatch
COPY --chown=cyberwatch:cyberwatch target/cybAlert-0.0.1-SNAPSHOT.jar /app/cyberwatch.jar
USER cyberwatch
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/cyberwatch.jar"]
