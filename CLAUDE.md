# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Task Management System built with Spring Boot 3.2.1, implementing a REST API for managing users, projects, and tasks. The application demonstrates both JPA and JDBC data access patterns using Spring profiles.

## Build and Development Commands

**Build the project:**
```bash
./mvnw clean install
```

**Run the application:**
```bash
./mvnw spring-boot:run
```

**Run with specific profile (jdbc or jpa):**
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=jdbc
./mvnw spring-boot:run -Dspring-boot.run.profiles=jpa
```

**Run tests:**
```bash
./mvnw test
```

**Run specific test class:**
```bash
./mvnw test -Dtest=UserIntegrationTests
```

**Access H2 Console (when running):**
- URL: http://localhost:8080/h2-console
- JDBC URL: jdbc:h2:mem:testdb
- Driver: org.h2.Driver

**Access Swagger UI (when running):**
- URL: http://localhost:8080/swagger-ui/index.html

## Architecture

### Multi-Implementation Pattern with Spring Profiles

The application implements **two separate data access strategies** that can be switched via Spring profiles:

1. **JPA Profile** (`spring.profiles.active=jpa`) - Default
   - Uses Spring Data JPA with entity classes
   - Located in: `implementation/jpa/`
   - Service classes annotated with `@Profile("jpa")`
   - Entities map to database tables via JPA annotations

2. **JDBC Profile** (`spring.profiles.active=jdbc`)
   - Uses JdbcTemplate for direct SQL queries
   - Located in: `implementation/jdbc/`
   - Service classes annotated with `@Profile("jdbc")`
   - Custom RowMapper classes for result set mapping

**Both implementations** provide the same functionality through shared service interfaces.

### Package Structure

```
sk.streetofcode.taskmanagementsystem/
├── api/                          # Service interfaces and request DTOs
│   ├── exception/                # Custom exceptions
│   ├── request/                  # API request models
│   ├── ProjectService.java       # Service interface
│   ├── TaskService.java
│   └── UserService.java
├── controller/                   # REST controllers
│   ├── ProjectController.java
│   ├── TaskController.java
│   └── UserController.java
├── domain/                       # Domain models (immutable with @Value)
│   ├── Project.java
│   ├── Task.java
│   ├── TaskStatus.java
│   └── User.java
└── implementation/
    ├── jpa/                     # JPA implementation
    │   ├── entity/              # JPA entities (@Entity)
    │   ├── repository/          # Spring Data repositories
    │   └── service/             # Service implementations (@Profile("jpa"))
    └── jdbc/                    # JDBC implementation
        ├── mapper/              # RowMapper classes
        ├── repository/          # Repository classes using JdbcTemplate
        └── service/             # Service implementations (@Profile("jdbc"))
```

### Domain Model

- **User**: Base entity with id, name, email
- **Project**: Belongs to a User, has name, description, createdAt
- **Task**: Belongs to a User, optionally belongs to a Project, has status (TaskStatus enum)

**Relationships:**
- User 1:N Projects
- User 1:N Tasks
- Project 1:N Tasks (optional)

### Key Design Decisions

1. **Immutable Domain Models**: Domain objects use Lombok's `@Value` annotation for immutability
2. **Service Interface Pattern**: Controllers depend on service interfaces, not implementations
3. **Profile-Based Implementation Selection**: Spring profiles (`jpa` or `jdbc`) determine which implementation is used at runtime
4. **Exception Handling**: Custom exceptions (ResourceNotFoundException, BadRequestException, InternalErrorException) are used throughout
5. **No Auto-DDL**: `spring.jpa.hibernate.ddl-auto=none` - schema is managed via `schema.sql`

## Database

- **Development**: H2 in-memory database
- **Supported**: PostgreSQL (driver included)
- **Schema**: Defined in `src/main/resources/schema.sql`
- **Initial Data**: Defined in `src/main/resources/data.sql`

## Testing

- Integration tests extend `IntegrationTest` base class
- Uses `TestRestTemplate` for REST API testing
- Tests run with `@SpringBootTest(webEnvironment = RANDOM_PORT)`
- Database context reset with `@DirtiesContext` after each test class

**Test structure:**
- `IntegrationTest.java` - Base class with common setup
- `UserIntegrationTests.java` - User API tests
- `ProjectIntegrationTests.java` - Project API tests
- `TaskIntegrationTests.java` - Task API tests

## API Documentation

The API is documented using SpringDoc OpenAPI (Swagger). All controllers use `@ApiResponses` annotations to document response codes.

## Common Patterns When Adding Features

**Adding a new entity:**
1. Create domain model in `domain/`
2. Create JPA entity in `implementation/jpa/entity/`
3. Create JPA repository in `implementation/jpa/repository/`
4. Create JDBC RowMapper in `implementation/jdbc/mapper/`
5. Create JDBC repository in `implementation/jdbc/repository/`
6. Create service interface in `api/`
7. Create both service implementations with correct `@Profile` annotations
8. Create controller in `controller/`
9. Update `schema.sql` and `data.sql`
10. Write integration tests

**When modifying existing features:**
- Remember to update **both** JPA and JDBC implementations
- Verify changes work with both profiles: `jpa` and `jdbc`
- Update integration tests to cover new behavior
