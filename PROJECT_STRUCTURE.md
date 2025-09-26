# Project Structure (Option 2: Layer-first within bounded contexts)

This structure organizes the modular monolith by architectural layers while preserving clear bounded contexts for each feature domain.

## Directory Layout

```
swifteats-backend/
  src/
    main/
      java/
        com/swifteats/
          interfaces/                  # Edge adapters exposed to clients
            api/                       # REST controllers, request/response models
              order/
              restaurant/
              location/
              user/
              notification/
            websocket/                 # STOMP endpoints, channel interceptors, message mappings

          application/                 # Application layer (use-cases and orchestration)
            order/
            restaurant/
            location/
            user/
            notification/

          domain/                      # Pure domain model and business rules
            order/
            restaurant/
            location/
            user/
            notification/
            common/

          infrastructure/              # Technical adapters and runtime integrations
            persistence/
              order/
              restaurant/
              location/
              user/
              notification/
            messaging/                  # RabbitMQ topology, producers/consumers wiring
            caching/                    # Redis caches, key spaces, cache managers
            security/                   # Authentication, authorization, RBAC artifacts
            config/                     # Spring Boot configuration classes and beans

      resources/
        application.yml                 # Base configuration
        application-local.yml           # Local development overrides
        application-prod.yml            # Production overrides
        db/migration/                   # Flyway versioned migrations

    test/
      java/                            # Tests mirroring main package structure
      resources/                        # Test resources and seed data

  build.gradle | pom.xml               # Build definition
  docker/                              # Docker Compose and environment files
  scripts/                             # Developer convenience scripts
```

## Module and Folder Purpose

- `interfaces/`: Exposes the system to external clients. Contains HTTP and WebSocket entry points and their protocol-specific models.
  - `interfaces/api/`: HTTP-facing endpoints grouped by bounded context (`order`, `restaurant`, `location`, `user`, `notification`).
  - `interfaces/websocket/`: WebSocket/STOMP endpoints and related message routing components.

- `application/`: Encapsulates application-specific workflows and use-cases. Coordinates domain operations and external interactions.
  - One subpackage per bounded context to keep use-cases grouped with their domain.

- `domain/`: Contains the domain model and business rules without external technology dependencies.
  - Context packages (`order`, `restaurant`, `location`, `user`, `notification`) hold entities, value objects, and domain services.
  - `domain/common/` contains shared domain primitives and types used across contexts.

- `infrastructure/`: Provides technical implementations for persistence, messaging, caching, security, and configuration.
  - `infrastructure/persistence/`: Data access adapters grouped by context.
  - `infrastructure/messaging/`: Message broker topology definitions and message channel bindings.
  - `infrastructure/caching/`: Cache configurations, key space definitions, and cache managers.
  - `infrastructure/security/`: Security-related components such as authentication and role enforcement.
  - `infrastructure/config/`: Centralized configuration classes and bean definitions.

- `resources/`: Application configuration and database migration assets.
  - `application*.yml`: Environment-specific configuration files.
  - `db/migration/`: Versioned database schema migrations.

- `test/`: Test sources mirroring the main structure with supporting resources.

- `docker/`: Container orchestration files for local and environment setups.

- `scripts/`: Utility scripts to assist with common project tasks.
