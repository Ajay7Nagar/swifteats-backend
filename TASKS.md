# TASKS

Scope: Organize development work into focused tracks with mini-backlogs. Check items off as they are completed.

---

## Track A: Users

- [x] Define `User` roles and states (CUSTOMER, DRIVER, RESTAURANT, ADMIN; ACTIVE/INACTIVE)
- [x] Database model for users (id, email, password hash, role, status, timestamps)
- [x] API endpoints: register, login, refresh, logout, get current user
- [x] Validation rules (email format, password non-empty)
- [x] JWT token issuance and verification (access + refresh tokens)
- [x] Role-based access checks (authenticated access to protected endpoints)
- [x] Error model alignment (401 for auth failures, 400 for bad requests)
- [x] Basic unit + integration tests (happy paths and common failures)
- [x] API documentation updates (captured in API-SPECIFICATION.yml)

---

## Track B: Restaurants

- [ ] Define `Restaurant` and `MenuItem` data models and constraints
- [ ] Database migrations for restaurants, menus, and relationships
- [ ] API endpoints: list/search restaurants, get restaurant by id, get menu
- [ ] Restaurant status updates (OPEN/CLOSED/BUSY) endpoint
- [ ] Caching strategy for menu and status (keys, TTLs)
- [ ] Cache invalidation on updates (menu/status changes)
- [ ] Pagination and filtering parameters for browse endpoints
- [ ] Performance checks for P99 ≤ 200ms (warm/cold cache runs)
- [ ] API documentation updates

---

## Track C: Orders

- [ ] Define `Order` and `OrderItem` models, lifecycle states, invariants
- [ ] Database migrations for orders, items, and audit trail
- [ ] API endpoints: create order, get order, update status
- [ ] Idempotency for order creation (Idempotency-Key)
- [ ] Mock payment processing integration and outcomes
- [ ] Driver assignment placeholder (to be integrated with Track E)
- [ ] Validation (availability, pricing snapshot, totals)
- [ ] Throughput validation target (≥500 orders/min sustained)
- [ ] API documentation updates

---

## Track D: Browse

- [ ] Endpoints: list/search restaurants (text + optional city filter), get menu
- [ ] Response shaping for quick render (essential fields, consistent sorting)
- [ ] Caching: cache-aside for menus; prewarm strategy
- [ ] Pagination defaults and limits
- [ ] Observability: latency metrics (P50/P95/P99), cache hit ratio
- [ ] Performance test plan execution (P99 ≤ 200ms)
- [ ] API documentation updates

---

## Track E: Driver stream + live updates

- [ ] Define driver location payload (lat/lon/accuracy/timestamp)
- [ ] API endpoint: ingest driver location
- [ ] Queue topology for ingestion (exchanges/queues/DLQ policy)
- [ ] Batch persistence approach (write cadence, batch size)
- [ ] Latest location lookup endpoint and small history API
- [ ] Live updates channel (SSE/WebSocket topics and routing)
- [ ] Ingestion capacity validation (50 drivers × 10 events/sec, backlog bounded)
- [ ] API documentation updates

---

## Cross-Cutting (applies across tracks)

- [ ] Error response format consistency
- [ ] Metrics and health endpoints coverage
- [ ] Test data seed and fixtures
- [ ] Documentation sync: README, API-SPECIFICATION.yml, ARCHITECTURE.md
