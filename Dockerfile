# Use Maven to build the application
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy all files
COPY . .

# Ensure mvnw is executable
RUN chmod +x mvnw

# Build the application
RUN ./mvnw clean package -DskipTests

# Use a lightweight JRE for running the app
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"] 