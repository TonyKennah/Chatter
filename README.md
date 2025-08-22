# Chatter

Chatter is a simple real-time chat application.

## Tech Stack

The application is built with a Java and Spring Boot backend, managed with Maven.

*   **Backend**: Java, Spring Boot
*   **Build Tool**: Maven

## Running the Application

To run the application, you will need Java and Maven installed. You can start the application using the following command from the project's root directory. This command runs the application with the `local` Spring profile active, which is useful for local development.

```shell
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=local"
```