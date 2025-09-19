#!/bin/bash

# Define colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# --- Configuration ---
CMD_SERVICE_URL="http://localhost:8080/api/v1"
QUERY_SERVICE_URL="http://localhost:8081/api/v1"
WAIT_TIME=10 # seconds to wait for services to start
MAX_RETRIES=5 # Max retries for service health checks
RETRY_INTERVAL=2 # seconds between retries

# --- Helper Functions ---
log_test() {
    echo -e "${BLUE}Testing: $1${NC}"
}

log_info() {
    echo -e "${YELLOW}$1${NC}"
}

log_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

log_failure() {
    echo -e "${RED}✗ $1${NC}"
    FAILED_TESTS=$((FAILED_TESTS + 1))
}

check_service_health() {
    local service_name=$1
    local url=$2
    local expected_status=$3
    local retries=0
    log_info "Waiting for $service_name to start at $url..."
    while [ $retries -lt $MAX_RETRIES ]; do
        HTTP_CODE=$(curl -s -o /dev/null -w '%{http_code}' "$url")
        if [ "$HTTP_CODE" -eq "$expected_status" ]; then
            log_success "$service_name is up and running (HTTP $HTTP_CODE)"
            return 0
        else
            echo -e "${YELLOW}  $service_name not ready (HTTP $HTTP_CODE). Retrying in $RETRY_INTERVAL seconds...${NC}"
            sleep $RETRY_INTERVAL
            retries=$((retries + 1))
        fi
    done
    log_failure "$service_name failed to start after $MAX_RETRIES retries."
    return 1
}

assert_status_code() {
    local response_code=$1
    local expected_code=$2
    local test_name=$3
    if [ "$response_code" -eq "$expected_code" ]; then
        log_success "$test_name (HTTP $response_code)"
    else
        log_failure "$test_name - Expected HTTP $expected_code, got HTTP $response_code"
    fi
}

# --- Main Test Execution ---
echo -e "${BLUE}=== Distributed Inventory System - Smoke Tests ===${NC}"
echo ""

# Initialize counters
FAILED_TESTS=0

# Check if services are healthy
log_test "Service Health Checks"
check_service_health "Stock Command Service" "$CMD_SERVICE_URL/health" 200 || exit 1
check_service_health "Stock Query Service" "$QUERY_SERVICE_URL/products" 200 || exit 1

echo ""

# Test 1: Create a new product
log_test "Create Product"
PRODUCT_DATA='{"name": "Gaming Laptop", "description": "High-performance gaming laptop", "category": "Electronics", "price": 1299.99, "storeId": "store1", "initialAmount": 5}'
RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X POST "$CMD_SERVICE_URL/products" -H "Content-Type: application/json" -d "$PRODUCT_DATA")
HTTP_CODE=$(echo "$RESPONSE" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
BODY=$(echo "$RESPONSE" | sed -e 's/HTTPSTATUS:.*//g')

if [ "$HTTP_CODE" -eq 201 ]; then
    PRODUCT_ID=$(echo "$BODY" | grep -o '"id":"[^"]*' | cut -d'"' -f4)
    log_success "Product created successfully with ID: $PRODUCT_ID"
else
    log_failure "Product creation failed (HTTP $HTTP_CODE): $BODY"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

echo ""

# Wait for event processing
log_info "Waiting for event processing..."
sleep 5

# Test 2: Query all products
log_test "Query All Products"
RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" "$QUERY_SERVICE_URL/products")
HTTP_CODE=$(echo "$RESPONSE" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
BODY=$(echo "$RESPONSE" | sed -e 's/HTTPSTATUS:.*//g')

if [ "$HTTP_CODE" -eq 200 ]; then
    PRODUCT_COUNT=$(echo "$BODY" | grep -o '"id":"[^"]*' | wc -l)
    log_success "Found $PRODUCT_COUNT products in the system"
else
    log_failure "Query all products failed (HTTP $HTTP_CODE): $BODY"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

echo ""

# Test 3: Update stock (if product was created successfully)
if [ -n "$PRODUCT_ID" ]; then
    log_test "Update Stock - Purchase Operation"
    STOCK_UPDATE_DATA='{"productId": "'$PRODUCT_ID'", "operation": "PURCHASE", "amount": 10, "reason": "Inventory restock"}'
    RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X PUT "$CMD_SERVICE_URL/products" -H "Content-Type: application/json" -d "$STOCK_UPDATE_DATA")
    HTTP_CODE=$(echo "$RESPONSE" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    BODY=$(echo "$RESPONSE" | sed -e 's/HTTPSTATUS:.*//g')

    if [ "$HTTP_CODE" -eq 200 ]; then
        log_success "Stock updated successfully"
    else
        log_failure "Stock update failed (HTTP $HTTP_CODE): $BODY"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi

    echo ""

    # Wait for event processing
    log_info "Waiting for event processing..."
    sleep 5

    # Test 4: Verify stock update
    log_test "Verify Stock Update"
    RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" "$QUERY_SERVICE_URL/products/$PRODUCT_ID")
    HTTP_CODE=$(echo "$RESPONSE" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    BODY=$(echo "$RESPONSE" | sed -e 's/HTTPSTATUS:.*//g')

    if [ "$HTTP_CODE" -eq 200 ]; then
        CURRENT_STOCK=$(echo "$BODY" | grep -o '"amount":[0-9]*' | cut -d':' -f2)
        if [ -n "$CURRENT_STOCK" ] && [ "$CURRENT_STOCK" -eq 15 ]; then
            log_success "Stock correctly updated to $CURRENT_STOCK"
        else
            log_failure "Expected stock 15, but got $CURRENT_STOCK"
            FAILED_TESTS=$((FAILED_TESTS + 1))
        fi
    else
        log_failure "Product query failed (HTTP $HTTP_CODE): $BODY"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
fi

echo ""

# Test 5: Create another product with different category
log_test "Create Second Product"
PRODUCT_DATA2='{"name": "Office Chair", "description": "Ergonomic office chair", "category": "Furniture", "price": 299.99, "storeId": "store2", "initialAmount": 20}'
RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" -X POST "$CMD_SERVICE_URL/products" -H "Content-Type: application/json" -d "$PRODUCT_DATA2")
HTTP_CODE=$(echo "$RESPONSE" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
BODY=$(echo "$RESPONSE" | sed -e 's/HTTPSTATUS:.*//g')

if [ "$HTTP_CODE" -eq 201 ]; then
    PRODUCT_ID2=$(echo "$BODY" | grep -o '"id":"[^"]*' | cut -d'"' -f4)
    log_success "Second product created successfully with ID: $PRODUCT_ID2"
else
    log_failure "Second product creation failed (HTTP $HTTP_CODE): $BODY"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

echo ""

# Wait for event processing
log_info "Waiting for event processing..."
sleep 5

# Test 6: Final query to verify both products
log_test "Final Product Count Verification"
RESPONSE=$(curl -s -w "HTTPSTATUS:%{http_code}" "$QUERY_SERVICE_URL/products")
HTTP_CODE=$(echo "$RESPONSE" | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
BODY=$(echo "$RESPONSE" | sed -e 's/HTTPSTATUS:.*//g')

if [ "$HTTP_CODE" -eq 200 ]; then
    FINAL_PRODUCT_COUNT=$(echo "$BODY" | grep -o '"id":"[^"]*' | wc -l)
    if [ "$FINAL_PRODUCT_COUNT" -ge 2 ]; then
        log_success "Final verification: Found $FINAL_PRODUCT_COUNT products in the system"
    else
        log_failure "Expected at least 2 products, but found $FINAL_PRODUCT_COUNT"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
else
    log_failure "Final product query failed (HTTP $HTTP_CODE): $BODY"
    FAILED_TESTS=$((FAILED_TESTS + 1))
fi

echo ""

# --- Summary ---
echo -e "${BLUE}=== Test Summary ===${NC}"
if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}✓ All smoke tests passed successfully!${NC}"
    echo -e "${GREEN}✓ Stock Management System is working correctly.${NC}"
    exit 0
else
    echo -e "${RED}✗ $FAILED_TESTS test(s) failed.${NC}"
    echo -e "${RED}✗ Stock Management System needs attention.${NC}"
    exit 1
fi