FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY frontend ./frontend
COPY src ./src
RUN mvn clean package -DskipTests

# Step 2: Run the app
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/chatter-0.0.1-SNAPSHOT.jar ../app.jar
RUN ls -lh / && ls -lh /app
ENTRYPOINT ["java","-jar","/app.jar"]