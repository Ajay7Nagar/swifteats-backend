.PHONY: help build test run docker-build docker-up docker-down docker-restart docker-logs docker-ps verify-persistence

help:
	@echo "Targets:"
	@echo "  build                - Gradle build (bootJar)"
	@echo "  test                 - Run tests with coverage (JaCoCo)"
	@echo "  run                  - Run Spring Boot locally"
	@echo "  docker-build         - Build Docker image"
	@echo "  docker-up            - Start stack with docker-compose"
	@echo "  docker-down          - Stop stack and keep volumes"
	@echo "  docker-restart       - Restart app container"
	@echo "  docker-logs          - Tail app logs"
	@echo "  docker-ps            - Show compose services"
	@echo "  verify-persistence   - Restart services and show volumes"

build:
	./gradlew clean bootJar

test:
	./gradlew clean test jacocoTestReport jacocoTestCoverageVerification

run:
	./gradlew bootRun

docker-build:
	docker build -t swifteats-backend:local .

docker-up:
	docker compose up -d --build

docker-down:
	docker compose down

docker-restart:
	docker compose restart api

docker-logs:
	docker compose logs -f api

docker-ps:
	docker compose ps

verify-persistence:
	@echo "Restarting services to verify data persistence..."
	docker compose restart postgres redis rabbitmq || true
	@echo "Listing named volumes:"
	docker volume ls | grep swifteats || true
	@echo "If application data remains after restart, persistence works (pgdata, redisdata, rabbitmqdata)."


