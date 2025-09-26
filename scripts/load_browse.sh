#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
source scripts/env.sh

N=${N:-100}
URL="$BASE_URL/restaurants"

OUT=/tmp/browse_times.txt
: > "$OUT"
ok=0
for i in $(seq 1 $N); do
  read -r code t < <(curl -s -o /dev/null -w "%{http_code} %{time_total}" "$URL" || echo "000 0")
  ms=$(awk -v t="$t" 'BEGIN{printf "%d", (t*1000)}')
  echo "$ms $code" >> "$OUT"
  [[ "$code" == "200" ]] && ok=$((ok+1))
done

sort -n "$OUT" | awk '{print $1}' > /tmp/browse_sorted.txt
total=$(wc -l < /tmp/browse_sorted.txt | tr -d ' ')
idx50=$(( (total*50)/100 ))
idx95=$(( (total*95)/100 ))
idx99=$(( (total*99)/100 ))
P50=$(awk -v k=$((idx50+1)) 'NR==k{print; exit}' /tmp/browse_sorted.txt)
P95=$(awk -v k=$((idx95+1)) 'NR==k{print; exit}' /tmp/browse_sorted.txt)
P99=$(awk -v k=$((idx99+1)) 'NR==k{print; exit}' /tmp/browse_sorted.txt)

echo "browse count=$N ok=$ok p50_ms=${P50:-0} p95_ms=${P95:-0} p99_ms=${P99:-0}"
echo "note: target NFR-3 p99 < 200ms (local indicative)"
