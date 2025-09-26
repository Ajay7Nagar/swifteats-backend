#!/usr/bin/env bash
set -euo pipefail
trap 'code=$?; echo "ERROR: script failed (exit $code)" | tee -a "$SUMMARY" >&2; exit $code' ERR

# Run browse, driver, and orders load tests with simple percentile reporting.
# Bounded loops only; no infinite waits.

cd "$(dirname "$0")/.."
source scripts/env.sh

N_BROWSE=${N_BROWSE:-100}
N_DRIVER=${N_DRIVER:-100}
N_ORDERS=${N_ORDERS:-100}
SUMMARY=/tmp/load_all_summary.txt
: > "$SUMMARY"

note() {
  echo "$*" | tee -a "$SUMMARY"
}

# Curl wrapper with bounded timeouts to avoid hangs
CURL_MAX_TIME=${CURL_MAX_TIME:-5}
CURL="curl -s --max-time ${CURL_MAX_TIME}"

# Return "code time_total" safely without failing script
curl_code_time() {
  local method=${1:-GET}
  local url=${2}
  local data=${3:-}
  if [[ "$method" == "GET" ]]; then
    $CURL -o /dev/null -w "%{http_code} %{time_total}" "$url" 2>/dev/null || echo "000 0"
  else
    $CURL -o /dev/null -w "%{http_code} %{time_total}" -X "$method" -H 'Content-Type: application/json' -d "$data" "$url" 2>/dev/null || echo "000 0"
  fi
}

percentiles() {
  # Args: file-with-ms-per-line label
  local file="$1"; shift
  local label="$1"; shift || true
  if [[ ! -s "$file" ]]; then
    note "$label: count=0 (no data)"
    return 0
  fi
  local total idx50 idx95 idx99 P50 P95 P99
  total=$(wc -l < "$file" | tr -d ' ')
  idx50=$(( (total*50)/100 ))
  idx95=$(( (total*95)/100 ))
  idx99=$(( (total*99)/100 ))
  P50=$(awk -v k=$((idx50+1)) 'NR==k{print; exit}' "$file")
  P95=$(awk -v k=$((idx95+1)) 'NR==k{print; exit}' "$file")
  P99=$(awk -v k=$((idx99+1)) 'NR==k{print; exit}' "$file")
  note "$label: count=$total p50_ms=${P50:-0} p95_ms=${P95:-0} p99_ms=${P99:-0}"
}

now_utc_iso() {
  # Portable UTC timestamp
  date -u +%Y-%m-%dT%H:%M:%SZ
}

ensure_health() {
  local url="${BASE_URL}/actuator/health"
  for i in {1..30}; do
    if curl -sf "$url" >/dev/null; then
      return 0
    fi
    sleep 1
  done
  echo "ERROR: health check failed at $url" >&2
  return 1
}

note "Checking health at ${BASE_URL}/actuator/health"
ensure_health

note "=== BROWSE LOAD (N=${N_BROWSE}) ==="
URL_BROWSE="$BASE_URL/restaurants"
OUT_BROWSE=/tmp/browse_times.txt
: > "$OUT_BROWSE"
ok_browse=0
for i in $(seq 1 "$N_BROWSE"); do
  out=$(curl_code_time GET "$URL_BROWSE")
  code=${out%% *}
  t=${out#* }
  if [[ -z "$t" ]]; then t=0; fi
  ms=$(awk -v t="$t" 'BEGIN{printf "%d", (t*1000)}')
  echo "$ms" >> "$OUT_BROWSE"
  [[ "$code" == "200" ]] && ok_browse=$((ok_browse+1))
done
percentiles "$OUT_BROWSE" "BROWSE"
note "BROWSE_OK=$ok_browse/$N_BROWSE"

note "=== SMOKE (order + driver online) ==="
SMOKE_OUT=/tmp/smoke_for_load.txt
bash scripts/smoke.sh | tee "$SMOKE_OUT" >/dev/null
ORDER_ID=$(grep -E "^ORDER_ID=" "$SMOKE_OUT" | tail -1 | cut -d= -f2)
if [[ -z "${ORDER_ID:-}" ]]; then
  echo "ERROR: failed to obtain ORDER_ID from smoke" >&2
  exit 1
fi
note "ORDER_ID=$ORDER_ID"

note "=== DRIVER LOAD (N=${N_DRIVER}) ==="
OUT_DRIVER=/tmp/times_driver.txt
: > "$OUT_DRIVER"
ok_driver=0
URL_DRIVER="$BASE_URL/drivers/$DRIVER_ID/location"
for i in $(seq 1 "$N_DRIVER"); do
  NOW=$(now_utc_iso)
  payload=$(cat <<JSON
{"orderId":"$ORDER_ID","lat":18.52,"lng":73.86,"timestamp":"$NOW"}
JSON
)
  out=$(curl_code_time POST "$URL_DRIVER" "$payload")
  code=${out%% *}
  t=${out#* }
  if [[ -z "$t" ]]; then t=0; fi
  ms=$(awk -v t="$t" 'BEGIN{printf "%d", (t*1000)}')
  echo "$ms" >> "$OUT_DRIVER"
  [[ "$code" == "202" || "$code" == "200" ]] && ok_driver=$((ok_driver+1))
done
percentiles "$OUT_DRIVER" "DRIVER"
note "DRIVER_OK=$ok_driver/$N_DRIVER"

note "=== ORDERS LOAD (N=${N_ORDERS}) ==="
OUT_ORDERS=/tmp/times_orders.txt
: > "$OUT_ORDERS"
ok_orders=0
URL_ORDERS="$BASE_URL/orders"
ORDER_PAYLOAD=$(cat <<JSON
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
for i in $(seq 1 "$N_ORDERS"); do
  out=$(curl_code_time POST "$URL_ORDERS" "$ORDER_PAYLOAD")
  code=${out%% *}
  t=${out#* }
  if [[ -z "$t" ]]; then t=0; fi
  ms=$(awk -v t="$t" 'BEGIN{printf "%d", (t*1000)}')
  echo "$ms" >> "$OUT_ORDERS"
  [[ "$code" == "201" ]] && ok_orders=$((ok_orders+1))
done
percentiles "$OUT_ORDERS" "ORDERS"
note "ORDERS_OK=$ok_orders/$N_ORDERS"

note "=== NOTES ==="
note "- NFR-3 target: browse/menu P99 < 200ms (local indicative)"
note "- NFR-1 target: orders 500/min sustained (this shows per-request latency only)"
note "- NFR-4 target: driver stream 2,000 events/sec (this shows per-request latency only)"


