FROM eclipse-temurin:25-jre
#
WORKDIR /app

# Create non-root user
RUN groupadd -r appgroup && useradd -m -r -g appgroup appuser

# Copy jar built by GitHub Actions
COPY build/libs/*.jar app.jar

RUN chown -R appuser:appgroup /app
USER appuser

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

EXPOSE 8080

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
