#!/usr/bin/env bash
set -euo pipefail

# Base configuration for local testing
export BASE_URL="${BASE_URL:-http://localhost:8080}"
export DRIVER_ID="${DRIVER_ID:-20000000-0000-0000-0000-000000000001}"
export RESTAURANT_ID="${RESTAURANT_ID:-00000000-0000-0000-0000-000000000001}"
export MENU_ITEM_ID="${MENU_ITEM_ID:-10000000-0000-0000-0000-000000000001}"

# Obtain JWT for test user if not provided (kept optional so browse endpoints work unauthenticated)
if [[ -z "${AUTH_TOKEN:-}" ]]; then
  AUTH_TOKEN=$(curl -s -X POST "$BASE_URL/auth/login" -H 'Content-Type: application/json' -d '{"email":"test@swifteats.local","password":"test123"}' | jq -r .token || true)
  if [[ "$AUTH_TOKEN" == "null" || -z "$AUTH_TOKEN" ]]; then
    AUTH_TOKEN=""
  fi
fi
export AUTH_TOKEN

if [[ "${VERBOSE:-0}" == "1" ]]; then
  echo "BASE_URL=$BASE_URL"
  echo "DRIVER_ID=$DRIVER_ID"
  echo "RESTAURANT_ID=$RESTAURANT_ID"
  echo "MENU_ITEM_ID=$MENU_ITEM_ID"
  echo "AUTH_TOKEN=${AUTH_TOKEN:+set}"
fi


