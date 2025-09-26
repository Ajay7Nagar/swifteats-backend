#!/usr/bin/env python3
import asyncio
import aiohttp
import os
import random
import time
import json
from uuid import UUID
from datetime import datetime, timezone

BASE_URL = os.getenv("BASE_URL", "http://localhost:8080")
NUM_DRIVERS = int(os.getenv("NUM_DRIVERS", "10"))
# EVENTS_PER_SEC is total events per second across all drivers
EVENTS_PER_SEC = float(os.getenv("EVENTS_PER_SEC", "5"))
DURATION = int(os.getenv("DURATION", "60"))
ORDER_ID_ENV = os.getenv("ORDER_ID", "")

DEFAULT_RESTAURANT_ID = os.getenv("RESTAURANT_ID", "00000000-0000-0000-0000-000000000001")
DEFAULT_MENU_ITEM_ID = os.getenv("MENU_ITEM_ID", "10000000-0000-0000-0000-000000000001")
DRIVER_IDS_ENV = os.getenv("DRIVER_IDS", "")
DRIVER_ID_ENV = os.getenv("DRIVER_ID", "20000000-0000-0000-0000-000000000001")

def now_iso_z() -> str:
    return datetime.now(timezone.utc).isoformat().replace("+00:00", "Z")

async def post_json(session: aiohttp.ClientSession, url: str, payload: dict) -> aiohttp.ClientResponse:
    return await session.post(url, json=payload)

async def ensure_order(session: aiohttp.ClientSession) -> str:
    if ORDER_ID_ENV:
        return ORDER_ID_ENV
    payload = {
        "customerId": "00000000-0000-0000-0000-000000000009",
        "restaurantId": DEFAULT_RESTAURANT_ID,
        "currency": "INR",
        "items": [{
            "menuItemId": DEFAULT_MENU_ITEM_ID,
            "nameSnapshot": "Fixture Item",
            "quantity": 1,
            "unitPrice": 100.0,
            "totalPrice": 100.0
        }]
    }
    async with await post_json(session, f"{BASE_URL}/orders", payload) as resp:
        text = await resp.text()
        try:
            data = json.loads(text)
            return data.get("orderId", "")
        except Exception:
            return ""

async def set_driver_online(session: aiohttp.ClientSession, driver_id: str) -> None:
    payload = {"status": "online"}
    async with await post_json(session, f"{BASE_URL}/drivers/{driver_id}/status", payload) as _:
        pass

async def send_location(session: aiohttp.ClientSession, driver_id: str, order_id: str) -> int:
    lat = 18.52 + random.uniform(-0.05, 0.05)
    lng = 73.86 + random.uniform(-0.05, 0.05)
    payload = {
        "orderId": order_id,
        "lat": lat,
        "lng": lng,
        "timestamp": now_iso_z()
    }
    async with await post_json(session, f"{BASE_URL}/drivers/{driver_id}/location", payload) as resp:
        # Count accepted as 200/202
        return resp.status

async def run():
    async with aiohttp.ClientSession(timeout=aiohttp.ClientTimeout(total=10)) as session:
        # Prepare driver IDs: prefer env-provided list/ID; otherwise fall back to synthetic
        if DRIVER_IDS_ENV.strip():
            drivers = [d.strip() for d in DRIVER_IDS_ENV.split(',') if d.strip()]
        elif DRIVER_ID_ENV:
            drivers = [DRIVER_ID_ENV]
        else:
            drivers = [str(UUID(int=20000000000000000000000000000001 + i)) for i in range(NUM_DRIVERS)]
        # Ensure order exists
        order_id = await ensure_order(session)
        if not order_id:
            print("SIM: failed to create or obtain ORDER_ID")
            return
        # Set all drivers online (best-effort)
        await asyncio.gather(*[set_driver_online(session, d) for d in drivers])

        # Compute interval so that total events/sec ~= EVENTS_PER_SEC
        # We send one update per driver per tick → total rate = len(drivers) / interval
        # Therefore, interval = len(drivers) / EVENTS_PER_SEC (min 0.05s, max 5s)
        interval = max(0.05, min(5.0, len(drivers) / max(0.1, EVENTS_PER_SEC)))

        end = time.time() + DURATION
        attempted = 0
        accepted = 0
        code_counts = {}
        while time.time() < end:
            statuses = await asyncio.gather(*[send_location(session, d, order_id) for d in drivers])
            for s in statuses:
                attempted += 1
                code_counts[s] = code_counts.get(s, 0) + 1
                if s in (200, 202):
                    accepted += 1
            await asyncio.sleep(interval)

        print(json.dumps({
            "attempted": attempted,
            "accepted": accepted,
            "success_ratio": (accepted / attempted) if attempted else 0.0,
            "num_drivers": NUM_DRIVERS,
            "events_per_sec_target": EVENTS_PER_SEC,
            "duration_sec": DURATION,
            "interval_sec": interval,
            "status_counts": code_counts
        }))

if __name__ == '__main__':
    asyncio.run(run())
