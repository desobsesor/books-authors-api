# Books and Authors API
<p align="center">
  <img src="api/src/main/resources/images/logo-min.png" alt="Database Schema" width="800"/>
</p>
  <p align="center">
  
A RESTful API for managing books and authors with a PL/SQL backend.</p>
<p align="center">
  <a href="https://www.java.com" target="_blank"><img src="https://img.shields.io/badge/Java-17-ED8B00?style=flat&logo=java&logoColor=white" alt="Java"></a>
  <a href="https://spring.io/" target="_blank"><img src="https://img.shields.io/badge/Spring_Boot-3.2.0-6DB33F?style=flat&logo=spring&logoColor=white" alt="Spring Boot"></a>
  <a href="https://maven.apache.org/" target="_blank"><img src="https://img.shields.io/badge/Maven-3.8-C71A36?style=flat&logo=apache-maven&logoColor=white" alt="Maven"></a>
  <a href="https://site.mockito.org" target="_blank"><img src="https://img.shields.io/badge/Mockito-5.0-83B81A?style=flat&logo=java&logoColor=white" alt="Mockito"></a>
  <a href="https://www.oracle.com/database/" target="_blank"><img src="https://img.shields.io/badge/Oracle-Database-%23F80000?style=flat&logo=oracle&logoColor=white" alt="Oracle Database"></a>
  <a href="https://springdoc.org/" target="_blank"><img src="https://img.shields.io/badge/OpenAPI-3.0-%2385EA2D?style=flat&logo=swagger&logoColor=white" alt="OpenAPI"></a>
  <a href="https://www.docker.com/" target="_blank"><img src="https://img.shields.io/badge/Docker-Containerized-%232496ED?style=flat&logo=docker&logoColor=white" alt="Docker"></a>
</p>

## Project Architecture

This project follows a clean hexagonal architecture with the following modules:

- **domain**: Contains the business entities, value objects, and repository interfaces
- **infrastructure**: Contains the implementation of repository interfaces, database configuration, and external services
- **application**: Contains the business logic, use cases, and service implementations
- **api**: Contains the REST controllers, DTOs, and API configuration

## Technology Stack

- Java 17
- Spring Boot 3.2.0
- Maven (multi-module project)
- Oracle Database with PL/SQL
- OpenAPI for API documentation
- JUnit 5 and Mockito for testing
- Docker and Docker Compose for containerization

## Getting Started

### Prerequisites

- JDK 17 or higher
- Maven 3.8 or higher
- Oracle Database instance with PL/SQL support

### Building the Project

```bash
mvn clean install
```

### Running the Application

```bash
mvn spring-boot:run -pl api
```

### API Documentation

Once the application is running, you can access the OpenAPI documentation at:

```
http://localhost:8080/swagger-ui/index.html
```

## Database Setup

The application requires an Oracle database with PL/SQL procedures. The database scripts are located in the `infrastructure/src/main/resources/db/scripts` directory.

## Docker Setup

### Configuration Structure

The Docker setup includes:

1. **Dockerfile**: Defines the build of the image for the Java Spring Boot application
2. **docker-compose.yml**: Orchestrates the services for the application and the Oracle database
3. **Initialization scripts**: Set up the Oracle database with tables, PL/SQL procedures, and test data

### Prerequisites

- Docker and Docker Compose installed on your system
- Internet access to download Docker images
- Credentials for the Oracle container registry (for the Oracle Database image)

### 1. Log in to the Oracle Container Registry

To access the Oracle Database image, you must first register at [Oracle Container Registry](https://container-registry.oracle.com) and accept the license terms for the Oracle Database Express Edition image.

```bash
docker login container-registry.oracle.com
```

### 2. Start services with Docker Compose

```bash
docker-compose up -d
```

This command will start:
- An Oracle Database XE container (21.3.0)
- A container for the Spring Boot application

### 3. Check the status of the services

```bash
docker-compose ps
```

### 4. Access the application

Once both containers are running:

- REST API: http://localhost:8080/
- Swagger Documentation: http://localhost:8080/swagger-ui.html

### 5. Access the Oracle database

```bash
docker exec -it oracle-books-db sqlplus Maya/Maya@//localhost:1521/XE
```

### Persistence configuration

Oracle data is stored in a Docker volume called `oracle-data`, ensuring data persists between container restarts.

### Stop the services

```bash
docker-compose down
```

To also remove the volumes (this will delete all persistent data):

```bash
docker-compose down -v
```

### Troubleshooting

#### Check application logs

```bash
docker-compose logs books-api
```

#### Check database logs

```bash
docker-compose logs oracle-db
```

#### Restart services

```bash
docker-compose restart
```

### Important notes

- The first initialization of Oracle Database may take several minutes
- The Spring Boot application is configured to wait for the database to be ready before starting
- The database credentials are set as:
  - User: Maya
  - Password: Maya

## Testing

To run the tests:

```bash
mvn test
```

## Author ✒️

_Built by_

- **Yovany Suárez Silva** - _Full Stack Software Engineer_ - [desobsesor](https://github.com/desobsesor)
- Website - [https://desobsesor.github.io/portfolio-web](https://desobsesor.github.io/portfolio-web/)

