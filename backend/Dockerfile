FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Resolve dependencies in their own layer so code changes don't re-download them.
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw -B -q dependency:go-offline

COPY src src
RUN ./mvnw -B clean package -DskipTests

FROM eclipse-temurin:25-jre

WORKDIR /app

RUN useradd --system --uid 1001 spring
USER spring

COPY --from=build /app/target/*.jar app.jar

# Render injects PORT at runtime; application.yaml falls back to SERVER_PORT / 8080.
EXPOSE 8080

# Size the heap from the container limit (Render instances have little RAM).
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
