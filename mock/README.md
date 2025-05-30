# Dockerized Mock Environment for Camel Application Testing

## 1. Overview

This directory (`mock/`) contains the Docker configurations for setting up mock external services required for testing the migrated Apache Camel application (`mulesoft-migrated-app`). The mock environment includes:

*   An **ActiveMQ Classic** message broker.
*   A **PostgreSQL** database server.

These services are orchestrated using Docker Compose.

## 2. Prerequisites

To use this mock environment, you need to have the following installed on your system:

*   **Docker:** [Install Docker](https://docs.docker.com/get-docker/)
*   **Docker Compose:** [Install Docker Compose](https://docs.docker.com/compose/install/) (Often included with Docker Desktop)

## 3. Services Provided

### 3.1. ActiveMQ

*   **Docker Image:** `rmohr/activemq:5.15.9-alpine` (A popular, well-maintained image for ActiveMQ Classic).
*   **Exposed Ports:**
    *   `61616` (Host) : `61616` (Container) - For OpenWire TCP connections from the Camel application.
    *   `8161` (Host) : `8161` (Container) - For the ActiveMQ Web Console.
*   **Web Console:** Accessible at `http://localhost:8161/`.
*   **Default Credentials (for Web Console):** `admin` / `admin` (This is the default for the `rmohr/activemq` image unless changed via environment variables in the Dockerfile, which we haven't).
*   **Container Name:** `mock-activemq`

### 3.2. PostgreSQL

*   **Docker Image:** `postgres:13-alpine` (Official PostgreSQL image).
*   **Exposed Port:**
    *   `5432` (Host) : `5432` (Container) - Standard PostgreSQL port.
*   **Database Configuration (as per `mock/database/Dockerfile` and `docker-compose.yml`):**
    *   **User:** `testuser`
    *   **Password:** `testpassword`
    *   **Database Name:** `testdb`
*   **Container Name:** `mock-postgresdb`

## 4. Database Initialization

*   The PostgreSQL service is initialized using the `init.sql` script located in the `mock/database/` directory.
*   This script is automatically executed by the `postgres` Docker image when the container starts for the first time (or when the data volume is empty).
*   It creates the `traditions` table:
    ```sql
    CREATE TABLE IF NOT EXISTS traditions (
        ID INT PRIMARY KEY,
        City VARCHAR(255),
        Tradition VARCHAR(255)
    );
    ```
*   You can add sample data to `init.sql` if needed for testing scenarios.

## 5. Usage

Convenience shell scripts are provided in this `mock/` directory to manage the lifecycle of the mock services. Ensure these scripts are executable (`chmod +x *.sh`).

### 5.1. Starting the Mock Environment

*   To build the images (if they don't exist locally or if Dockerfiles have changed) and start the services in detached mode (background):
    ```bash
    ./start-mocks.sh
    ```
    This script executes `docker-compose up -d --build`.

### 5.2. Stopping the Mock Environment

*   To stop and remove the containers (networks and volumes defined in `docker-compose.yml` might also be removed depending on `docker-compose down` options, by default it removes containers and networks but not named volumes):
    ```bash
    ./stop-mocks.sh
    ```
    This script executes `docker-compose down`.

### 5.3. Checking Logs

*   To view the logs from the running services (e.g., for troubleshooting):
    ```bash
    # View logs for ActiveMQ
    docker-compose logs -f activemq

    # View logs for PostgreSQL
    docker-compose logs -f postgresdb
    ```
    Use `Ctrl+C` to stop tailing the logs.

## 6. Connecting the Camel Application

To configure the `mulesoft-migrated-app` Camel application (or its tests) to connect to these Dockerized mock services:

1.  **Activate the `dockertest` Spring Profile:**
    *   When running the Spring Boot application or specific integration tests, activate the `dockertest` profile. This can be done by:
        *   Setting the environment variable: `SPRING_PROFILES_ACTIVE=dockertest`
        *   Using a JVM system property: `-Dspring.profiles.active=dockertest`
        *   For JUnit 5 tests, annotating the test class with `@ActiveProfiles("dockertest")`.

2.  **Configuration Properties:**
    *   The connection properties for these mock services are defined in the `src/test/resources/application-dockertest.yml` file within the main Camel application project. This file includes:
        *   ActiveMQ broker URL: `tcp://localhost:61616`
        *   PostgreSQL JDBC URL: `jdbc:postgresql://localhost:5432/testdb` (with user `testuser` and password `testpassword`).

By using the `dockertest` profile, the application will use these specific configurations to communicate with the services running in Docker.
