[![Build & Test Status](https://github.com/TonyKennah/Chatter/actions/workflows/maven.yml/badge.svg)](https://github.com/TonyKennah/Chatter/actions/workflows/maven.yml)

# Chatter

Chatter is a simple real-time chat application designed for auto-scaling environments. It uses WebSockets for instant, two-way communication between clients and the server.

## Tech Stack

The application is built with a Java and Spring Boot backend, managed with Maven.

*   **Backend**: Java, Spring Boot
*   **Real-time Communication**: Spring WebSockets
*   **Database**: MongoDB (for cloud/production environments), H2 (for local development)
*   **Build Tool**: Maven

## Continuous Integration

This project uses [GitHub Actions](https://github.com/TonyKennah/Chatter/actions) for Continuous Integration. The build status badge at the top of this README reflects the status of the latest build on the `main` branch.

## Real-Time Communication

The application uses WebSockets to facilitate real-time interaction. This allows for instant delivery of messages and other events without relying on traditional HTTP polling.

The WebSocket connection handles:
*   Sending and receiving text messages.
*   Broadcasting user actions, such as when a user is actively typing a message.

## Auto-Scaling and Instance Identity

Chatter is designed to work in modern, auto-scaling cloud environments. When a new instance of the application starts, it needs a way to uniquely identify itself.

On startup, each instance registers a unique "room" in the MongoDB database. This allows the frontend application to discover all active instances and present them as available chat rooms for users to join.

The instance identity is determined using the following priority:
1.  A Java System Property: `-Dinstance.id=my-unique-id`
2.  An Environment Variable: `INSTANCE_ID`
3.  A fallback to the machine's hostname.

This mechanism ensures that each server instance, whether running locally for development or as part of a larger auto-scaling group in the cloud, has a distinct identity.

## Running the Application

To run the application, you will need Java and Maven installed. Here are some common ways to start the application from the project's root directory:

### Basic Run
```shell
mvn spring-boot:run
```


```shell
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=local"
```

```shell
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dinstance.id='Gnome (cli)'"
```

```shell
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=local -Dspring-boot.run.jvmArguments="-Dinstance.id='Gnome (cli)'"
```

```shell
mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.profiles.active=local" "-Dspring-boot.run.jvmArguments='-Dinstance.id=Gnome'"
```