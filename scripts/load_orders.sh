#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/env.sh

N=${N:-100}
url="$BASE_URL/orders"
payload=$(cat <<JSON
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

touch /tmp/times_orders.txt
: > /tmp/times_orders.txt
ok=0
for i in $(seq 1 $N); do
  read -r code t < <(curl -s -o /dev/null -w "%{http_code} %{time_total}" -X POST "$url" -H 'Content-Type: application/json' -d "$payload" || echo "000 0")
  ms=$(awk -v t="$t" 'BEGIN{printf "%d", (t*1000)}')
  echo $ms >> /tmp/times_orders.txt
  [[ "$code" == "201" ]] && ok=$((ok+1))
done

sort -n /tmp/times_orders.txt > /tmp/times_orders_sorted.txt
total=$(wc -l < /tmp/times_orders_sorted.txt | tr -d ' ')
idx50=$(( (total*50)/100 ))
idx95=$(( (total*95)/100 ))
idx99=$(( (total*99)/100 ))
P50=$(awk -v k=$((idx50+1)) 'NR==k{print; exit}' /tmp/times_orders_sorted.txt)
P95=$(awk -v k=$((idx95+1)) 'NR==k{print; exit}' /tmp/times_orders_sorted.txt)
P99=$(awk -v k=$((idx99+1)) 'NR==k{print; exit}' /tmp/times_orders_sorted.txt)

echo "orders count=$N ok=$ok p50_ms=${P50:-0} p95_ms=${P95:-0} p99_ms=${P99:-0}"
echo "note: target NFR-1 is throughput 500/min sustained; this shows latency only"


