# Fleet Management System - Quick Start Script

echo "🚀 Fleet Management System - Quick Start"
echo "========================================="
echo ""

# Check prerequisites
echo "📋 Checking prerequisites..."

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    echo "✅ Java: $JAVA_VERSION"
else
    echo "❌ Java not found. Please install Java 17 or higher."
    exit 1
fi

# Check Maven
if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1)
    echo "✅ Maven: $MVN_VERSION"
else
    echo "❌ Maven not found. Please install Maven 3.8+."
    exit 1
fi

# Check Docker
if command -v docker &> /dev/null; then
    DOCKER_VERSION=$(docker --version)
    echo "✅ Docker: $DOCKER_VERSION"
else
    echo "❌ Docker not found. Please install Docker Desktop."
    exit 1
fi

echo ""
echo "🐳 Starting Docker services (Kafka & PostgreSQL)..."
cd docker
docker-compose up -d

echo ""
echo "⏳ Waiting for services to start (30 seconds)..."
sleep 30

# Check if Kafka is ready
echo "🔍 Checking Kafka..."
if docker exec kafka kafka-broker-api-versions --bootstrap-server localhost:9092 &> /dev/null; then
    echo "✅ Kafka is ready"
else
    echo "⚠️  Kafka not ready yet, waiting another 10 seconds..."
    sleep 10
fi

# Check if PostgreSQL is ready
echo "🔍 Checking PostgreSQL..."
if docker exec fleet-postgres pg_isready -U fleet_user &> /dev/null; then
    echo "✅ PostgreSQL is ready"
else
    echo "⚠️  PostgreSQL not ready yet, waiting another 10 seconds..."
    sleep 10
fi

echo ""
echo "📊 Creating Kafka topics..."
docker exec kafka kafka-topics --create \
  --topic raw_telemetry \
  --bootstrap-server localhost:9092 \
  --partitions 6 \
  --replication-factor 1 \
  --if-not-exists

docker exec kafka kafka-topics --create \
  --topic normalized_telemetry \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1 \
  --if-not-exists

docker exec kafka kafka-topics --create \
  --topic driving_events \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1 \
  --if-not-exists

echo "✅ Kafka topics created"

echo ""
echo "🏗️  Building all services..."
cd ..
mvn clean install -DskipTests

echo ""
echo "✅ Build complete!"
echo ""
echo "📝 Next steps:"
echo ""
echo "1. Start services in separate terminals:"
echo "   Terminal 1: cd telemetry-ingestion-service && mvn spring-boot:run"
echo "   Terminal 2: cd event-processing-service && mvn spring-boot:run"
echo "   Terminal 3: cd driver-scoring-service && mvn spring-boot:run"
echo ""
echo "2. Test the system:"
echo "   curl -X POST http://localhost:8081/api/telemetry -H 'Content-Type: application/json' -d @docs/sample-telemetry.json"
echo ""
echo "3. View results:"
echo "   Kafka UI: http://localhost:8080"
echo "   pgAdmin: http://localhost:5050 (admin@fleet.com / admin)"
echo ""
echo "4. Check driver data:"
echo "   curl http://localhost:8083/api/driver/DRV-ABC-123/score"
echo ""
echo "📚 Documentation: See README.md and docs/ folder"
echo ""
