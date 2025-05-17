FROM eclipse-temurin:17-jdk-jammy as builder
WORKDIR /nba-players-statistics
COPY . .
RUN ./gradlew build -x test

FROM eclipse-temurin:17-jre-jammy
WORKDIR /nba-players-statistics
COPY --from=builder /nba-players-statistics/build/libs/*.jar nba-players-statistics-1.0-SNAPSHOT.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "nba-players-statistics-1.0-SNAPSHOT.jar"]