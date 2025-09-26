#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/env.sh

# Requires ORDER_ID to be set (e.g., from smoke.sh output)
if [[ -z "${ORDER_ID:-}" ]]; then
  echo "ERROR: ORDER_ID is not set. Run smoke.sh and export ORDER_ID, or set it manually." >&2
  exit 1
fi

N=${N:-100}
url="$BASE_URL/drivers/$DRIVER_ID/location"

touch /tmp/times_driver.txt
: > /tmp/times_driver.txt
ok=0
for i in $(seq 1 $N); do
  NOW=$(date -u +%Y-%m-%dT%H:%M:%SZ)
  read -r code t < <(curl -s -o /dev/null -w "%{http_code} %{time_total}" -X POST "$url" -H 'Content-Type: application/json' -d "{\"orderId\":\"$ORDER_ID\",\"lat\":18.52,\"lng\":73.86,\"timestamp\":\"$NOW\"}" || echo "000 0")
  ms=$(awk -v t="$t" 'BEGIN{printf "%d", (t*1000)}')
  echo $ms >> /tmp/times_driver.txt
  [[ "$code" == "202" ]] && ok=$((ok+1))
done

sort -n /tmp/times_driver.txt > /tmp/times_driver_sorted.txt
total=$(wc -l < /tmp/times_driver_sorted.txt | tr -d ' ')
idx50=$(( (total*50)/100 ))
idx95=$(( (total*95)/100 ))
idx99=$(( (total*99)/100 ))
P50=$(awk -v k=$((idx50+1)) 'NR==k{print; exit}' /tmp/times_driver_sorted.txt)
P95=$(awk -v k=$((idx95+1)) 'NR==k{print; exit}' /tmp/times_driver_sorted.txt)
P99=$(awk -v k=$((idx99+1)) 'NR==k{print; exit}' /tmp/times_driver_sorted.txt)

echo "driver_ingest count=$N ok=$ok p50_ms=${P50:-0} p95_ms=${P95:-0} p99_ms=${P99:-0}"
echo "note: target NFR-4 is 2,000 events/sec concurrent; this shows latency only"


