# SwiftEats Backend

Modular monolith backend (Java 17 + Spring Boot 3). JWT-based auth with user management.

## Prerequisites

- Java 17 (SDKMAN recommended)
- Docker and Docker Compose
- Make, curl, jq

Optional: SDKMAN to switch JDK
```
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk install java 17.0.10-tem
sdk use java 17.0.10-tem
```

## Build, Test, Coverage

```
make test
```
Generates JaCoCo HTML at `build/reports/jacoco/test/html/index.html`.

## Run Locally (H2 + in-memory deps)

```
make run
```
App at `http://localhost:8080`.

Health: `GET /actuator/health`

Swagger UI: `http://localhost:8080/swagger-ui.html`

OpenAPI JSON: `GET /v3/api-docs`

## Auth & Test User

- Register: `POST /auth/register`
- Login: `POST /auth/login` returns `{ token }`
- Default seeded user (for local/dev/scripts):
  - email: `test@swifteats.local`
  - password: `test123`

Swagger Authorize modal supports:
- bearer-jwt: paste JWT from `/auth/login`
- basic: username/password (if you enable Basic in security)

## Docker: Full Stack (Postgres, RabbitMQ, Redis, API)

Build application jar:
```
./gradlew clean bootJar
```

Build image:
```
make docker-build
```

Start stack:
```
make docker-up
```

Stop stack (keep volumes):
```
make docker-down
```

Useful:
```
make docker-logs    # tail API logs
make docker-restart # restart API container
make docker-ps      # list services
```

Ports:
- API: 8080
- Postgres: 5432
- Redis: 6379
- RabbitMQ: 5672 (AMQP), 15672 (Mgmt)

## Smoke Test

With app running (local or docker), run:
```
bash scripts/smoke.sh
```
It will:
- wait for health
- browse restaurants and menu
- create an order (uses JWT if available)
- transition the order
- post and fetch driver location

## Load Tests (simple curl-based)

Browse:
```
bash scripts/load_browse.sh
```

Orders (uses JWT if available):
```
bash scripts/load_orders.sh
```

Driver ingest (requires ORDER_ID exported from smoke output):
```
ORDER_ID=<id from smoke>
bash scripts/load_driver.sh
```

All together:
```
bash scripts/load_all.sh
```

You can override envs via `scripts/env.sh` or environment variables:
```
BASE_URL=http://localhost:8080 N=200 bash scripts/load_orders.sh
```

## Security Summary

- Open endpoints: `GET /restaurants/**`, `POST /auth/register`, `POST /auth/login`, `/actuator/health`, `/actuator/info`, Swagger (`/swagger-ui/**`, `/v3/api-docs/**`).
- All other endpoints require `Authorization: Bearer <JWT>`.

## Project Structure

- domain: business models and interfaces
- application: services
- adapters/web: controllers
- adapters/persistence: JPA entities and repositories
- adapters/messaging: payment queue worker
- config: beans, security, OpenAPI
- infrastructure/config: seeds and infra configs

## API Spec

- Swagger UI at `/swagger-ui.html`.
- OpenAPI file: `API-SPECIFICATION.yml`.
