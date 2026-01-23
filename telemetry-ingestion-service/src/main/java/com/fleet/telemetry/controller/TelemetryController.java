package com.fleet.telemetry.controller;

import com.fleet.telemetry.model.TelemetryData;
import com.fleet.telemetry.service.TelemetryProducer;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Telemetry REST Controller
 * 
 * This REST endpoint receives telemetry data from trucks and sends it to Kafka.
 * 
 * Key Learning Points:
 * - REST API as entry point for data
 * - Converting HTTP requests to Kafka messages
 * - Immediate response (async processing)
 */
@RestController
@RequestMapping("/api/telemetry")
@Slf4j
public class TelemetryController {

    private final TelemetryProducer telemetryProducer;

    public TelemetryController(TelemetryProducer telemetryProducer) {
        this.telemetryProducer = telemetryProducer;
    }

    /**
     * Receive telemetry data from trucks
     * 
     * POST /api/telemetry/ingest
     * 
     * Example request body:
     * {
     *   "truckId": "TRUCK-001",
     *   "speed": 75.5,
     *   "timestamp": 1702638600000
     * }
     * 
     * This demonstrates:
     * - Receiving data via REST
     * - Immediately sending to Kafka (fire-and-forget)
     * - Returning success response without waiting for Kafka confirmation
     */
    @PostMapping("/ingest")
    public ResponseEntity<Map<String, String>> ingestTelemetry(@Valid @RequestBody TelemetryData telemetryData) {
        log.info(
                "📥 Received telemetry data - Truck: {}, Speed: {} km/h, Driver: {}, Fuel Level: {}, Engine Temp: {}, Location: {}, Tire Pressure: {}",
                telemetryData.getTruckId(),
                telemetryData.getSpeed(),
                telemetryData.getDriverId(),
                telemetryData.getFuelLevel(),
                telemetryData.getEngineTemp(),
                telemetryData.getLocation(),
                telemetryData.getTirePressure());

        // Set timestamp if not provided
        if (telemetryData.getTimestamp() == null) {
            telemetryData.setTimestamp(System.currentTimeMillis());
        }
        
        // Send to Kafka (fire-and-forget)
        telemetryProducer.sendTelemetry(telemetryData);
        
        // Return immediate response
        Map<String, String> response = new HashMap<>();
        response.put("status", "accepted");
        response.put("message", "Telemetry data sent to processing pipeline");
        response.put("truckId", telemetryData.getTruckId());
        
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {

        try{

            TelemetryData invalidData = new TelemetryData(

            );

            invalidData.setSpeed(1000.3);

            Map<String, String> response = Map.of(
                    "status", "REJECTED",
                    "message", "Validation Not working"
            );

            return ResponseEntity.status(400).body(response);
        } catch (Exception exception) {
            log.error("Validation is working as expected: {}", exception.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("status", "UP");
            response.put("service", "Telemetry Ingestion Service");
            response.put("Validation", "Validation is working");
            return ResponseEntity.ok(response);
        }

    }
}
