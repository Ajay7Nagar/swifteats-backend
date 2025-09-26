# Non-Functional Test Plan (Local Environment)

Scope: Local validation of critical non-functional targets using the docker-compose environment and standard development hardware. Tests focus on browse performance (P99 ≤ 200ms), order throughput, driver location ingestion, resilience under dependency failures, and data simulation capacity (50 drivers × 10 events/second).

Environment assumptions:
- Single developer machine (≥2 CPU cores, ≥2GB RAM available for stack)
- All platform services started via docker-compose
- Test data pre-seeded (restaurants, menus, user accounts)
- Time synchronization is consistent across components during tests

Metrics sources (no code required):
- Application metrics (HTTP timers, custom timers) via metrics endpoint or logs
- Database statistics via DB monitoring dashboard or stats views
- Message broker metrics via management UI (queue depth, consumer rate, DLQ size)
- System metrics (CPU, memory) via OS tools or container stats

Data collection protocol:
- Warm-up period: 2–3 minutes before capturing metrics
- Measurement window: 10–15 minutes per scenario unless specified
- Report P50/P95/P99 latencies, throughput, error rate (≥HTTP 400/500)
- Capture supporting metrics snapshots at test start, midpoint, and end

---

## 1) Browse Performance – P99 ≤ 200ms

Objective: Validate that menu and restaurant browse operations meet P99 ≤ 200ms locally.

Endpoints under test:
- GET /api/restaurants (search, pagination)
- GET /api/restaurants/{restaurantId}/menu (menu and status)

Inputs:
- Dataset: ≥100 restaurants, each with ≥50 menu items
- Concurrency levels: 50, 200, 500 virtual users (stepwise)
- Mix: 70% GET /menu (random restaurants), 30% GET /restaurants (with/without query)
- Duration: 12 minutes (2 min warm-up + 10 min measurement)

Procedure:
1. Ensure caches are cold for the first step; record cold-start behavior.
2. Run workload at 50 VUs, then 200, then 500; maintain the traffic mix above.
3. Repeat menu test with warmed cache.

Expected results:
- P99 latency ≤ 200ms for both endpoints during measurement window
- Error rate < 1%
- Cache hit ratio ≥ 80% during warmed runs

Metrics to collect:
- Per-endpoint latency distribution (P50/P95/P99)
- Request throughput (RPS) and error rate
- Cache metrics: hit/miss ratio for menu keys
- DB read latency (average and P95) and connection pool utilization

Acceptance criteria:
- Pass when P99 ≤ 200ms for warmed runs and error rate < 1%

---

## 2) Order Processing Throughput

Objective: Validate sustained throughput of 500 orders/minute locally with acceptable latency.

Endpoint under test:
- POST /api/orders (idempotency-key provided per request)

Inputs:
- Payloads: 3–5 items per order with realistic prices; valid customer and restaurant IDs
- Rate: 500 orders/min (≈8.33 orders/sec)
- Duration: 30 minutes sustained (2 min warm-up + 28 min measurement)

Procedure:
1. Generate unique idempotency keys per order request.
2. Maintain constant rate of 8–10 orders/sec over measurement window.
3. Include realistic distribution of restaurants and items.

Expected results:
- Achieved throughput ≥ 500 orders/minute sustained
- P95 order creation latency ≤ 500ms, P99 ≤ 800ms
- No duplicate orders when idempotency keys are reused

Metrics to collect:
- Order create latency distribution and success rate
- DB write latency and row write rates for orders and order_items
- CPU and memory utilization; GC activity

Acceptance criteria:
- Pass when sustained throughput is ≥500 orders/min, P95 ≤ 500ms, error rate < 1%, zero duplicates

---

## 3) Driver Location Ingestion Capacity (Local Simulator)

Objective: Validate ingestion of up to 50 drivers generating 10 events/sec each.

Endpoints and queues under test:
- POST /api/drivers/{driverId}/location
- GPS ingestion queue (accept rate, depth, DLQ)

Inputs:
- 50 driver identities
- Event rate: 10 events/sec/driver (total ≈500 events/sec)
- Payload: latitude/longitude within Maharashtra bounds; timestamp monotonic per driver
- Duration: 10 minutes (2 min warm-up + 8 min measurement)

Procedure:
1. Start the local simulator producing events at the specified rate.
2. Monitor queue depth, consumer rate, application ingest rate, and DB batch write latency.
3. Verify latest location availability via GET /api/drivers/{driverId}/location/latest for random drivers.

Expected results:
- Ingest accept rate ≈ send rate (±5%)
- Average processing latency from ingress to persistence ≤ 150ms
- Queue depth remains bounded; DLQ size remains 0

Metrics to collect:
- HTTP accept rate (202) and latency for POST /location
- Queue depth, consumer throughput, requeue/negative-ack counts
- DB batch insert latency and write throughput for driver_locations

Acceptance criteria:
- Pass when accept rate matches send rate within ±5%, average ingest latency ≤150ms, DLQ size = 0

---

## 4) Resilience Under Dependency Failures

Objective: Validate graceful degradation and recovery when dependencies fail or degrade.

Scenarios and inputs:
1. Cache outage
   - Action: Temporarily stop the cache service.
   - Test: Run browse workload (Section 1 mix) at 200 VUs for 10 minutes.
   - Expected: Requests continue; P99 increases but stays within documented degraded SLO; cache errors logged; no crashes.

2. Message broker outage
   - Action: Temporarily stop the message broker service.
   - Test: Continue driver location POSTs at 500 events/sec.
   - Expected: API applies backpressure or 429 where configured; no data loss once broker is restored; backlog drains after recovery; DLQ remains 0.

3. Database write slowdown
   - Action: Introduce write latency (e.g., throttled IOPS environment).
   - Test: Run order creation at 8–10 orders/sec.
   - Expected: Request latency increases but no order data corruption; retries/backoff visible; system recovers when latency normalizes.

Metrics to collect:
- Request latency/error rate during failure windows
- Backlog depth (queues) and drain time after recovery
- Cache miss ratio and DB read latency during cache outage

Acceptance criteria:
- Pass when the application remains available for core reads, degrades gracefully for writes/ingest, and fully recovers with no data loss after dependency restoration

---

## 5) Data Simulation Capability (Local)

Objective: Validate the local simulator can produce up to 50 drivers × 10 events/sec and realistic order traffic.

Inputs:
- Driver count: 50
- Event rate: 10 events/sec/driver
- Duration: 10 minutes
- Optional: synthetic order traffic at 2–4 orders/sec for realism

Procedure:
1. Start simulator with specified parameters.
2. Observe system KPIs for ingest, queueing, and persistence.
3. Randomly query latest location for 10 drivers every minute.

Expected results:
- Simulator maintains configured event rate with ≤2% deviation
- System KPIs remain within thresholds defined in Sections 1–3

Metrics to collect:
- Produced vs accepted event counts, clock skew, event loss
- Latest location freshness (age) for sampled drivers

Acceptance criteria:
- Pass when simulator achieves target rate and system ingests without loss, with location freshness ≤ 2 seconds for sampled drivers

---

## Reporting Template

For each scenario, record:
- Test ID and description
- Environment snapshot (CPU, memory, versions)
- Inputs (rates, concurrency, data sizes), warm-up time, measurement window
- Metrics summary (latency percentiles, throughput, error rate, cache hit ratio, queue depth, DB latency)
- Observations (bottlenecks, anomalies)
- Result (Pass/Fail) and notes

