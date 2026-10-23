# Step 1: build the jar file using Maven
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q package -DskipTests

# Step 2: run the jar file using only Java
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/ums.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
