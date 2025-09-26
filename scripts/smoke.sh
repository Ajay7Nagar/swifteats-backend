#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/env.sh

echo "Waiting for health..."
for i in {1..30}; do curl -sf "$BASE_URL/actuator/health" >/dev/null && break || sleep 1; done
curl -s "$BASE_URL/actuator/health" | jq -r .status

echo "Browse restaurants"
curl -s "$BASE_URL/restaurants" | jq '.data | length'

echo "Fetch menu"
curl -s "$BASE_URL/restaurants/$RESTAURANT_ID/menu" | jq '.items | length'

echo "Create order"
ORDER_JSON=$(cat <<JSON
{
  "customerId": "00000000-0000-0000-0000-000000000009",
  "restaurantId": "$RESTAURANT_ID",
  "currency": "INR",
  "items": [
    {"menuItemId": "$MENU_ITEM_ID", "nameSnapshot": "Fixture Item", "quantity": 1, "unitPrice": 100.00, "totalPrice": 100.00}
  ]
}
JSON
)
AUTH_HEADER=()
if [[ -n "${AUTH_TOKEN:-}" ]]; then AUTH_HEADER=(-H "Authorization: Bearer $AUTH_TOKEN"); fi
CREATE_RESP=$(curl -s -X POST "$BASE_URL/orders" -H 'Content-Type: application/json' "${AUTH_HEADER[@]}" -d "$ORDER_JSON")
echo "$CREATE_RESP"
ORDER_ID=$(echo "$CREATE_RESP" | jq -r .orderId)
echo "ORDER_ID=$ORDER_ID"

echo "Get order"
curl -s "$BASE_URL/orders/$ORDER_ID" "${AUTH_HEADER[@]}" | jq '.id'

echo "Transition: confirm"
curl -s -X POST "$BASE_URL/orders/$ORDER_ID/transition" -H 'Content-Type: application/json' "${AUTH_HEADER[@]}" -d '{"action":"confirm"}' | jq -r .status

echo "Transition invalid (deliver from confirmed) should 409"
curl -s -o /dev/null -w "%{http_code}\n" -X POST "$BASE_URL/orders/$ORDER_ID/transition" -H 'Content-Type: application/json' "${AUTH_HEADER[@]}" -d '{"action":"deliver"}'

echo "Driver online"
curl -s -X POST "$BASE_URL/drivers/$DRIVER_ID/status" -H 'Content-Type: application/json' "${AUTH_HEADER[@]}" -d '{"status":"online"}' | jq -r .status

echo "Post location"
NOW=$(date -u +%Y-%m-%dT%H:%M:%SZ)
curl -s -X POST "$BASE_URL/drivers/$DRIVER_ID/location" -H 'Content-Type: application/json' "${AUTH_HEADER[@]}" -d "{\"orderId\":\"$ORDER_ID\",\"lat\":18.52,\"lng\":73.86,\"timestamp\":\"$NOW\"}" | jq -r .accepted

echo "Read driver location"
curl -s "$BASE_URL/orders/$ORDER_ID/driver-location" "${AUTH_HEADER[@]}" | jq '{driverId, orderId, lat, lng, timestamp}'

echo "Smoke OK"


