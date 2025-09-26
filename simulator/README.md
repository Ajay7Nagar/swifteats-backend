# SwiftEats Simulator

A minimal CLI to simulate driver location updates to a running API.

Config:
- number_of_drivers (default 10)
- events_per_second (default 5)
- duration_seconds (default 60)
- base_url (default http://localhost:8080)

---

## Quick Start

1) Ensure the API is running and healthy
- `curl -s http://localhost:8080/actuator/health` → expect `{ "status": "UP" }`

2) Create a Python virtual environment and install dependencies (one-time)
```bash
cd simulator
python3 -m venv .venv
source .venv/bin/activate
pip install --upgrade pip
pip install aiohttp
```

3) Run the simulator
- 50 drivers, ~10 events/sec total, for 60s:
```bash
source simulator/.venv/bin/activate
NUM_DRIVERS=50 EVENTS_PER_SEC=10 DURATION=60 python simulator/sim.py
```
- Strict ≥600 acceptance (slight oversubscription):
```bash
source simulator/.venv/bin/activate
NUM_DRIVERS=50 EVENTS_PER_SEC=10.5 DURATION=60 python simulator/sim.py
```

The simulator auto-creates an order, sets drivers online, and posts location updates. It prints a JSON summary with attempted, accepted, success_ratio, and status_counts.

## Environment Variables
- `BASE_URL` (default `http://localhost:8080`) – API base URL
- `NUM_DRIVERS` (default `10`) – number of concurrent drivers simulated
- `EVENTS_PER_SEC` (default `5`) – total events/second across all drivers
- `DURATION` (default `60`) – simulation duration in seconds
- `ORDER_ID` (optional) – use an existing order ID instead of auto-creating one
- `RESTAURANT_ID` (default `00000000-0000-0000-0000-000000000001`)
- `MENU_ITEM_ID` (default `10000000-0000-0000-0000-000000000001`)
- `DRIVER_IDS` (optional) – comma-separated list of driver IDs to use
- `DRIVER_ID` (optional) – single driver ID to use if `DRIVER_IDS` is not provided

## Notes
- The interval is computed so that total event rate ≈ `EVENTS_PER_SEC`.
- Responses 200/202 are counted as accepted.
- For FR-8 local validation, use 50 drivers and target ~10 events/sec for 60 seconds (≥ 600 accepted updates).
