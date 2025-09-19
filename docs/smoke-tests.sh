#!/usr/bin/env bash
set -euo pipefail

BASE_URL=${BASE_URL:-http://localhost:8080}
TOKEN=${TOKEN:-devtoken}
SKU=${SKU:-A1}
STORE=${STORE:-S1}

function uuid() { python3 - <<'PY'
import uuid; print(str(uuid.uuid4()))
PY
}

echo "[1/10] Health check"
curl -fsS "$BASE_URL/health" >/dev/null && echo "  OK"

echo "[2/10] List product totals"
curl -fsS "$BASE_URL/v1/products/totals" | sed -e 's/.*/  Received totals/'

echo "[3/10] Create product (lowest-stock store)"
IDEMP=$(uuid)
curl -fsS -X POST "$BASE_URL/v1/products"   -H "Content-Type: application/json"   -H "Authorization: Bearer $TOKEN"   -H "Idempotency-Key: $IDEMP"   -d "{"sku":"A2","name":"New Product","initialOnHand":10}" >/dev/null && echo "  Created"

echo "[4/10] Purchase SKU without store (policy highest-available)"
IDEMP=$(uuid)
curl -fsS -X POST "$BASE_URL/v1/purchase"   -H "Content-Type: application/json"   -H "Authorization: Bearer $TOKEN"   -H "Idempotency-Key: $IDEMP"   -d "{"sku":"$SKU","qty":1,"customerId":"smoke","policy":"highest-available"}" >/dev/null && echo "  Purchase accepted"

echo "[5/10] Adjust -1 for sale (explicit store)"
IDEMP=$(uuid)
curl -fsS -X POST "$BASE_URL/v1/adjustments"   -H "Content-Type: application/json"   -H "Authorization: Bearer $TOKEN"   -H "Idempotency-Key: $IDEMP"   -d "{"storeId":"$STORE","sku":"$SKU","delta":-1,"reason":"sale","traceId":"smoke"}"   | sed -e 's/.*/  Accepted/'

echo "[6/10] Read availability"
curl -fsS "$BASE_URL/v1/availability?sku=$SKU" | sed -e 's/.*/  Availability retrieved/'

echo "[7/10] Create reservation qty=1"
RES_ID="res_$(uuid | cut -d- -f1)"
IDEMP=$(uuid)
curl -fsS -X POST "$BASE_URL/v1/reservations"   -H "Content-Type: application/json"   -H "Authorization: Bearer $TOKEN"   -H "Idempotency-Key: $IDEMP"   -d "{"storeId":"$STORE","lines":[{"sku":"$SKU","qty":1}],"ttlSeconds":900,"customerId":"smoke"}"   >/dev/null && echo "  Reservation created (client-id: $RES_ID)"

echo "[8/10] Confirm reservation"
IDEMP=$(uuid)
curl -fsS -X POST "$BASE_URL/v1/reservations/$RES_ID:confirm"   -H "Content-Type: application/json"   -H "Authorization: Bearer $TOKEN"   -H "Idempotency-Key: $IDEMP"   -d "{"paymentRef":"smoke_pay"}" >/dev/null && echo "  Confirmed"

echo "[9/10] Read reservation"
curl -fsS "$BASE_URL/v1/reservations/$RES_ID" | sed -e 's/.*/  Retrieved/'

echo "[10/10] Read availability again"
curl -fsS "$BASE_URL/v1/availability?sku=$SKU" | sed -e 's/.*/  Availability retrieved/'

echo "Smoke tests completed."
