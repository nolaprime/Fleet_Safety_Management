#!/bin/bash

# Kafka Basics Demo - Test Script
# This script sends sample telemetry data to test the Kafka producer-consumer setup

echo "🚀 Kafka Basics Demo - Sending Test Telemetry Data"
echo "=================================================="
echo ""

# Color codes for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Base URL for the telemetry service
BASE_URL="http://localhost:8081"

# Check if service is running
echo "Checking if Telemetry Ingestion Service is running..."
if ! curl -s "${BASE_URL}/api/telemetry/health" > /dev/null 2>&1; then
    echo -e "${RED}❌ Error: Telemetry Ingestion Service is not running on port 8081${NC}"
    echo "Please start the service first:"
    echo "  cd telemetry-ingestion-service"
    echo "  mvn spring-boot:run"
    exit 1
fi
echo -e "${GREEN}✅ Service is running${NC}"
echo ""

# Function to send telemetry
send_telemetry() {
    local truck_id=$1
    local speed=$2
    local message=$3
    
    echo -e "${YELLOW}Sending:${NC} $message"
    
    response=$(curl -s -X POST "${BASE_URL}/api/telemetry/ingest" \
        -H "Content-Type: application/json" \
        -d "{
            \"truckId\": \"${truck_id}\",
            \"speed\": ${speed}
        }")
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ Sent successfully${NC}"
    else
        echo -e "${RED}❌ Failed to send${NC}"
    fi
    echo ""
    sleep 1
}

echo "📤 Sending test data to Kafka..."
echo ""

# Test 1: Normal speed
send_telemetry "TRUCK-001" 65.5 "Truck 001 at normal speed (65.5 km/h)"

# Test 2: Another normal speed
send_telemetry "TRUCK-002" 72.0 "Truck 002 at normal speed (72.0 km/h)"

# Test 3: Speed violation
send_telemetry "TRUCK-003" 95.0 "Truck 003 with SPEED VIOLATION (95.0 km/h)"

# Test 4: Another violation
send_telemetry "TRUCK-001" 88.5 "Truck 001 with SPEED VIOLATION (88.5 km/h)"

# Test 5: Low speed
send_telemetry "TRUCK-004" 45.0 "Truck 004 at low speed (45.0 km/h)"

echo "=================================================="
echo -e "${GREEN}✅ Test data sent successfully!${NC}"
echo ""
echo "💡 Check the Event Processing Service logs to see the messages being consumed"
echo ""
echo "You should see:"
echo "  - 📨 Received telemetry from Kafka messages"
echo "  - ⚠️  Speed violation warnings for trucks exceeding 80 km/h"
echo ""
