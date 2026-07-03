package com.argela.iot_device_management.controller;

import com.argela.iot_device_management.dto.TelemetryRequest;
import com.argela.iot_device_management.service.TelemetryService;
import jakarta.validation.Valid;
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
    public ResponseEntity<String> createTelemetry(@Valid @RequestBody TelemetryRequest request) {
        telemetryService.writeTelemetry(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Telemetry data saved.");
    }
}