-- Fleet Management System Database Schema
-- PostgreSQL 15+

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Table: drivers
CREATE TABLE drivers (
    id VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100),
    current_score INTEGER DEFAULT 100,
    total_trips INTEGER DEFAULT 0,
    total_miles DECIMAL(10, 2) DEFAULT 0.0,
    total_violations INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT score_range CHECK (current_score >= 0 AND current_score <= 100),
    CONSTRAINT miles_positive CHECK (total_miles >= 0),
    CONSTRAINT trips_positive CHECK (total_trips >= 0),
    CONSTRAINT violations_positive CHECK (total_violations >= 0)
);

CREATE INDEX idx_drivers_score ON drivers(current_score);
CREATE INDEX idx_drivers_name ON drivers(name);

-- Table: trips
CREATE TABLE trips (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    driver_id VARCHAR(50) NOT NULL REFERENCES drivers(id) ON DELETE CASCADE,
    truck_id VARCHAR(50) NOT NULL,
    device_id VARCHAR(50) NOT NULL,
    
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    duration_minutes INTEGER,
    
    start_location_lat DECIMAL(9, 6),
    start_location_lon DECIMAL(9, 6),
    end_location_lat DECIMAL(9, 6),
    end_location_lon DECIMAL(9, 6),
    
    total_miles DECIMAL(10, 2) DEFAULT 0.0,
    violation_count INTEGER DEFAULT 0,
    
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED')),
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT miles_positive CHECK (total_miles >= 0),
    CONSTRAINT duration_positive CHECK (duration_minutes IS NULL OR duration_minutes >= 0),
    CONSTRAINT violations_positive CHECK (violation_count >= 0)
);

CREATE INDEX idx_trips_driver ON trips(driver_id);
CREATE INDEX idx_trips_status ON trips(status);
CREATE INDEX idx_trips_start_time ON trips(start_time DESC);
CREATE INDEX idx_trips_truck ON trips(truck_id);

-- Table: violations
CREATE TABLE violations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trip_id UUID REFERENCES trips(id) ON DELETE SET NULL,
    driver_id VARCHAR(50) NOT NULL REFERENCES drivers(id) ON DELETE CASCADE,
    truck_id VARCHAR(50) NOT NULL,
    
    event_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH', 'CRITICAL')),
    
    location_lat DECIMAL(9, 6) NOT NULL,
    location_lon DECIMAL(9, 6) NOT NULL,
    
    points_deducted INTEGER NOT NULL,
    
    metadata JSONB,
    description TEXT,
    
    timestamp TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT points_positive CHECK (points_deducted >= 0)
);

CREATE INDEX idx_violations_driver ON violations(driver_id);
CREATE INDEX idx_violations_trip ON violations(trip_id);
CREATE INDEX idx_violations_timestamp ON violations(timestamp DESC);
CREATE INDEX idx_violations_type ON violations(event_type);
CREATE INDEX idx_violations_severity ON violations(severity);
CREATE INDEX idx_violations_metadata ON violations USING GIN (metadata);

-- Insert sample data for testing
INSERT INTO drivers (id, name, current_score, total_trips, total_miles, total_violations)
VALUES 
    ('DRV-ABC-123', 'John Doe', 100, 0, 0.0, 0),
    ('DRV-XYZ-456', 'Jane Smith', 100, 0, 0.0, 0),
    ('DRV-DEF-789', 'Bob Johnson', 100, 0, 0.0, 0);

-- Function to update driver statistics
CREATE OR REPLACE FUNCTION update_driver_stats()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        -- Update driver when new violation is added
        IF TG_TABLE_NAME = 'violations' THEN
            UPDATE drivers 
            SET 
                current_score = GREATEST(0, current_score - NEW.points_deducted),
                total_violations = total_violations + 1,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = NEW.driver_id;
        END IF;
        
        -- Update driver when trip is completed
        IF TG_TABLE_NAME = 'trips' AND NEW.status = 'COMPLETED' THEN
            UPDATE drivers 
            SET 
                total_trips = total_trips + 1,
                total_miles = total_miles + COALESCE(NEW.total_miles, 0),
                updated_at = CURRENT_TIMESTAMP
            WHERE id = NEW.driver_id;
        END IF;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers to automatically update driver statistics
CREATE TRIGGER trigger_update_driver_on_violation
    AFTER INSERT ON violations
    FOR EACH ROW
    EXECUTE FUNCTION update_driver_stats();

CREATE TRIGGER trigger_update_driver_on_trip
    AFTER INSERT OR UPDATE ON trips
    FOR EACH ROW
    WHEN (NEW.status = 'COMPLETED')
    EXECUTE FUNCTION update_driver_stats();

-- View: Driver summary with recent activity
CREATE OR REPLACE VIEW driver_summary AS
SELECT 
    d.id,
    d.name,
    d.current_score,
    d.total_trips,
    d.total_miles,
    d.total_violations,
    COUNT(DISTINCT t.id) FILTER (WHERE t.start_time > CURRENT_TIMESTAMP - INTERVAL '7 days') as trips_last_7_days,
    COUNT(DISTINCT v.id) FILTER (WHERE v.timestamp > CURRENT_TIMESTAMP - INTERVAL '7 days') as violations_last_7_days,
    MAX(t.start_time) as last_trip_time
FROM drivers d
LEFT JOIN trips t ON d.id = t.driver_id
LEFT JOIN violations v ON d.id = v.driver_id
GROUP BY d.id, d.name, d.current_score, d.total_trips, d.total_miles, d.total_violations;

-- Comments for documentation
COMMENT ON TABLE drivers IS 'Driver master table storing current state and aggregated statistics';
COMMENT ON TABLE trips IS 'Trip records with start/end times, locations, and mileage';
COMMENT ON TABLE violations IS 'Safety violations detected during trips with location and severity';

COMMENT ON COLUMN trips.status IS 'ACTIVE: trip in progress, COMPLETED: trip finished';
COMMENT ON COLUMN violations.metadata IS 'JSON field storing event-specific details like speed, acceleration, etc.';
