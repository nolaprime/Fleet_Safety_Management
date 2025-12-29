#!/bin/bash

# Fleet Management System - Docker Build and Run Script
# This script builds Docker images and starts all services via docker-compose

set -e  # Exit on error

echo "🚀 Fleet Management System - Docker Build & Run"
echo "================================================"
echo ""

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored messages
print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

# Check prerequisites
echo "📋 Checking prerequisites..."

# Check Docker
if ! command -v docker &> /dev/null; then
    print_error "Docker not found. Please install Docker Desktop."
    exit 1
fi
print_success "Docker: $(docker --version)"

# Check Docker Compose
if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose not found. Please install Docker Compose."
    exit 1
fi
print_success "Docker Compose: $(docker-compose --version)"

# Check if Docker daemon is running
if ! docker info &> /dev/null; then
    print_error "Docker daemon is not running. Please start Docker Desktop."
    exit 1
fi
print_success "Docker daemon is running"

echo ""
print_info "This script will:"
echo "  1. Build Maven artifacts locally"
echo "  2. Build Docker images for both services"
echo "  3. Start all services (Kafka, PostgreSQL, and microservices)"
echo ""

# Ask for confirmation
read -p "Continue? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    print_warning "Aborted by user"
    exit 0
fi

echo ""
echo "🏗️  Step 1: Building Maven artifacts..."
echo "========================================"

# Set Java 17 for build
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-20.jdk/Contents/Home

# Build using Maven wrapper or Maven
if [ -f "./mvnw.sh" ]; then
    print_info "Using Maven wrapper..."
    ./mvnw.sh clean package -DskipTests -pl telemetry-ingestion-service,event-processing-service -am
else
    print_info "Using system Maven..."
    mvn clean package -DskipTests -pl telemetry-ingestion-service,event-processing-service -am
fi

if [ $? -ne 0 ]; then
    print_error "Maven build failed"
    exit 1
fi

print_success "Maven build completed"

echo ""
echo "🐳 Step 2: Building Docker images..."
echo "====================================="

cd docker

# Build Docker images
print_info "Building telemetry-ingestion-service image..."
docker-compose build telemetry-ingestion-service

print_info "Building event-processing-service image..."
docker-compose build event-processing-service

print_success "Docker images built successfully"

echo ""
echo "🚀 Step 3: Starting all services..."
echo "===================================="

# Stop any existing containers
print_info "Stopping any existing containers..."
docker-compose down

# Start services
print_info "Starting services with docker-compose..."
docker-compose up -d

echo ""
print_info "Waiting for services to initialize..."
echo "  - Zookeeper and Kafka (30 seconds)..."
sleep 30

echo "  - Checking Kafka health..."
MAX_RETRIES=10
RETRY_COUNT=0
while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if docker exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092 &> /dev/null; then
        print_success "Kafka is ready"
        break
    fi
    RETRY_COUNT=$((RETRY_COUNT + 1))
    if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
        print_error "Kafka failed to start"
        print_info "Check logs with: docker-compose logs kafka"
        exit 1
    fi
    echo "  Waiting... (attempt $RETRY_COUNT/$MAX_RETRIES)"
    sleep 5
done

echo ""
echo "📊 Creating Kafka topics..."
docker exec kafka kafka-topics --create \
  --topic raw-telemetry \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1 \
  --if-not-exists &> /dev/null

print_success "Kafka topic 'raw-telemetry' created"

echo ""
echo "  - Waiting for microservices to start (20 seconds)..."
sleep 20

echo ""
echo "🔍 Checking service status..."
echo "=============================="

# Check container status
docker-compose ps

echo ""
echo "✅ Deployment Complete!"
echo "======================="
echo ""
echo "📝 Service URLs:"
echo "  • Telemetry Ingestion API:  http://localhost:8081"
echo "  • Kafka UI:                 http://localhost:8080"
echo "  • pgAdmin:                  http://localhost:5050 (admin@fleet.com / admin)"
echo ""
echo "🧪 Test the system:"
echo "  # Send a test telemetry message"
echo "  curl -X POST http://localhost:8081/api/telemetry/ingest \\"
echo "    -H 'Content-Type: application/json' \\"
echo "    -d '{\"truckId\": \"TRUCK-001\", \"speed\": 75.5}'"
echo ""
echo "  # Or use the test script"
echo "  cd .. && ./test-kafka-demo.sh"
echo ""
echo "📊 View logs:"
echo "  docker-compose logs -f                          # All services"
echo "  docker-compose logs -f telemetry-ingestion-service"
echo "  docker-compose logs -f event-processing-service"
echo ""
echo "🛑 Stop all services:"
echo "  docker-compose down"
echo ""
echo "🗑️  Remove all data and start fresh:"
echo "  docker-compose down -v"
echo ""
