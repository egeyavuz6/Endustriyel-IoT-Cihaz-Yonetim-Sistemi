package com.argela.iot_device_management.controller;

import com.argela.iot_device_management.service.TelemetryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class TelemetryController {

    private final TelemetryService telemetryService;

    public TelemetryController(TelemetryService telemetryService) {
        this.telemetryService = telemetryService;
    }

    @GetMapping("/api/devices/{id}/telemetry")
    public List<Map<String, Object>> getTelemetry(@PathVariable Long id) {
        return telemetryService.getTelemetryByDeviceId(id);
    }

    @GetMapping("/api/devices/{id}/telemetry/latest")
    public List<Map<String, Object>> getLatestTelemetry(@PathVariable Long id) {
        return telemetryService.getLatestTelemetry(id);
    }

    @GetMapping("/api/devices/{id}/telemetry/stats")
    public Map<String, Object> getTelemetryStats(@PathVariable Long id) {
        return telemetryService.getTelemetryStats(id);
    }

    @PostMapping("/api/telemetry")
    public ResponseEntity<String> createTelemetry(@RequestBody Map<String, Object> payload) {
        Long deviceId = Long.valueOf(payload.get("deviceId").toString());
        double temperature = Double.parseDouble(payload.get("temperature").toString());
        double humidity = Double.parseDouble(payload.get("humidity").toString());
        double pressure = Double.parseDouble(payload.get("pressure").toString());
        double vibration = Double.parseDouble(payload.get("vibration").toString());

        telemetryService.writeTelemetry(deviceId, temperature, humidity, pressure, vibration);

        return ResponseEntity.status(HttpStatus.CREATED).body("Telemetry data saved.");
    }
}