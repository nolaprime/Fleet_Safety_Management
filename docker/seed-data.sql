-- Sample seed data for Fleet Safety Management
-- Intended for local/dev environments

-- Drivers
INSERT INTO drivers (id, name, current_score, total_trips, total_miles, total_violations)
VALUES
    ('DRV-1001', 'Avery Turner', 100, 0, 0.0, 0),
    ('DRV-1002', 'Casey Nguyen', 100, 0, 0.0, 0),
    ('DRV-1003', 'Jordan Patel', 100, 0, 0.0, 0),
    ('DRV-1004', 'Riley Gomez', 100, 0, 0.0, 0),
    ('DRV-1005', 'Morgan Blake', 100, 0, 0.0, 0),
    ('DRV-1006', 'Taylor Brooks', 100, 0, 0.0, 0),
    ('DRV-1007', 'Quinn Rivera', 100, 0, 0.0, 0),
    ('DRV-1008', 'Sydney Cole', 100, 0, 0.0, 0);

-- Trips (COMPLETED trips will update driver totals via trigger)
INSERT INTO trips (
    driver_id,
    truck_id,
    device_id,
    start_time,
    end_time,
    duration_minutes,
    start_location_lat,
    start_location_lon,
    end_location_lat,
    end_location_lon,
    total_miles,
    violation_count,
    status
)
VALUES
    ('DRV-1001', 'TRK-201', 'DEV-01', '2026-02-10 08:15:00', '2026-02-10 09:05:00', 50,
     29.951065, -90.071533, 29.972500, -90.052900, 38.2, 1, 'COMPLETED'),
    ('DRV-1002', 'TRK-202', 'DEV-02', '2026-02-11 10:00:00', '2026-02-11 11:45:00', 105,
     29.952300, -90.066900, 29.985100, -90.095400, 64.7, 0, 'COMPLETED'),
    ('DRV-1003', 'TRK-203', 'DEV-03', '2026-02-12 07:20:00', '2026-02-12 08:10:00', 50,
     29.943200, -90.083100, 29.967800, -90.101200, 31.5, 2, 'COMPLETED'),
    ('DRV-1004', 'TRK-204', 'DEV-04', '2026-02-13 15:40:00', NULL, NULL,
    29.938400, -90.070700, NULL, NULL, 0.0, 0, 'ACTIVE'),
    ('DRV-1005', 'TRK-205', 'DEV-05', '2026-02-14 06:50:00', '2026-02-14 08:00:00', 70,
    29.947000, -90.079400, 29.985800, -90.066100, 46.3, 0, 'COMPLETED'),
    ('DRV-1006', 'TRK-206', 'DEV-06', '2026-02-15 12:10:00', '2026-02-15 13:25:00', 75,
    29.950200, -90.062500, 29.978300, -90.089900, 52.9, 1, 'COMPLETED'),
    ('DRV-1007', 'TRK-207', 'DEV-07', '2026-02-16 09:30:00', '2026-02-16 10:40:00', 70,
    29.935500, -90.073300, 29.968900, -90.104200, 44.8, 1, 'COMPLETED'),
    ('DRV-1008', 'TRK-208', 'DEV-08', '2026-02-17 16:05:00', NULL, NULL,
    29.940700, -90.065200, NULL, NULL, 0.0, 0, 'ACTIVE');

-- Violations (each insert updates driver score/violations via trigger)
INSERT INTO violations (
    driver_id,
    truck_id,
    event_type,
    severity,
    message,
    speed,
    fuel_level,
    engine_temp,
    location_lat,
    location_lon,
    front_right,
    front_left,
    back_right,
    back_left,
    points_deducted,
    metadata,
    description,
    timestamp
)
VALUES
    ('DRV-1001', 'TRK-201', 'SPEEDING', 'MEDIUM', 'Speed exceeded 75 mph', 82.5, 62.0, 208.4,
     29.960120, -90.063510, 35.2, 35.0, 34.8, 34.9, 6,
     '{"limitMph":75,"overByMph":7.5}',
     'Sustained speeding for 45 seconds on I-10',
     '2026-02-10 08:42:10'),
    ('DRV-1003', 'TRK-203', 'ENGINE_TEMP_HIGH', 'HIGH', 'Engine temperature exceeded safe threshold', 58.0, 48.5, 235.0,
     29.955500, -90.094600, 36.1, 36.0, 35.8, 35.9, 12,
     '{"thresholdF":225,"peakF":235}',
     'Engine temp high for 2 minutes; check coolant system',
     '2026-02-12 07:48:30'),
    ('DRV-1003', 'TRK-203', 'HARD_BRAKING', 'LOW', 'Harsh braking detected', 41.2, 47.8, 214.2,
     29.963900, -90.099200, 35.9, 35.7, 35.6, 35.6, 3,
     '{"decelG":-0.42}',
     'Sudden stop near Canal St intersection',
    '2026-02-12 07:56:05'),
    ('DRV-1006', 'TRK-206', 'TIRE_PRESSURE_LOW', 'MEDIUM', 'Front right tire pressure low', 52.4, 51.2, 218.6,
    29.962700, -90.074400, 30.5, 34.6, 34.8, 34.7, 5,
    '{"thresholdPsi":32,"observedPsi":30.5}',
    'Front right tire under-inflated for 3 minutes',
    '2026-02-15 12:48:15'),
    ('DRV-1007', 'TRK-207', 'SPEEDING', 'LOW', 'Speed exceeded 70 mph', 74.2, 58.0, 209.1,
    29.954600, -90.093700, 35.6, 35.5, 35.4, 35.4, 4,
    '{"limitMph":70,"overByMph":4.2}',
    'Brief speeding on US-61',
    '2026-02-16 10:05:40'),
    ('DRV-1007', 'TRK-207', 'FUEL_LEVEL_LOW', 'LOW', 'Fuel level below 15 percent', 46.5, 13.8, 205.7,
    29.961900, -90.098800, 35.4, 35.3, 35.2, 35.3, 2,
    '{"thresholdPct":15,"observedPct":13.8}',
    'Fuel level low near end of route',
    '2026-02-16 10:32:20');

-- Driver scores (optional snapshot data)
INSERT INTO driver_score (
    driver_id,
    current_score,
    score_category,
    total_violations,
    last_violation_date,
    updated_at
)
VALUES
    ('DRV-1001', 94, 'GOOD', 1, '2026-02-10 08:42:10', '2026-02-10 09:06:00'),
    ('DRV-1002', 100, 'EXCELLENT', 0, '2026-02-01 00:00:00', '2026-02-11 11:46:00'),
    ('DRV-1003', 85, 'FAIR', 2, '2026-02-12 07:56:05', '2026-02-12 08:11:00'),
    ('DRV-1004', 100, 'EXCELLENT', 0, '2026-02-01 00:00:00', '2026-02-13 15:41:00'),
    ('DRV-1005', 100, 'EXCELLENT', 0, '2026-02-01 00:00:00', '2026-02-14 08:01:00'),
    ('DRV-1006', 95, 'GOOD', 1, '2026-02-15 12:48:15', '2026-02-15 13:26:00'),
    ('DRV-1007', 94, 'GOOD', 2, '2026-02-16 10:32:20', '2026-02-16 10:41:00'),
    ('DRV-1008', 100, 'EXCELLENT', 0, '2026-02-01 00:00:00', '2026-02-17 16:06:00');
