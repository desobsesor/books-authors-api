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
http://localhost:8080/swagger-ui.html
```

## Database Setup

The application requires an Oracle database with PL/SQL procedures. The database scripts are located in the `infrastructure/src/main/resources/db/scripts` directory.

## Testing

To run the tests:

```bash
mvn test
```

## Author ✒️

_Built by_

- **Yovany Suárez Silva** - _Full Stack Software Engineer_ - [desobsesor](https://github.com/desobsesor)
- Website - [https://desobsesor.github.io/portfolio-web](https://desobsesor.github.io/portfolio-web/)

