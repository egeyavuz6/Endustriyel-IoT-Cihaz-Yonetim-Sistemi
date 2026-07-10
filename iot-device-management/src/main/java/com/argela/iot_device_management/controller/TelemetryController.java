package com.argela.iot_device_management.controller;

import com.argela.iot_device_management.dto.TelemetryRequest;
import com.argela.iot_device_management.entity.Device;
import com.argela.iot_device_management.service.DeviceService;
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
    private final DeviceService deviceService;

    public TelemetryController(TelemetryService telemetryService, DeviceService deviceService) {
        this.telemetryService = telemetryService;
        this.deviceService = deviceService;
    }

    @GetMapping("/api/devices/{id}/telemetry")
    public List<Map<String, Object>> getTelemetry(
            @PathVariable Long id,
            @RequestParam(defaultValue = "24") int hours) {
        return telemetryService.getTelemetryByDeviceId(id, hours);
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

    @GetMapping("/api/devices/serial/{serialNumber}/telemetry")
    public List<Map<String, Object>> getTelemetryBySerialNumber(
            @PathVariable String serialNumber,
            @RequestParam(defaultValue = "1") int hours,
            @RequestParam(required = false) String field) {
        Device device = deviceService.getDeviceBySerialNumber(serialNumber);
        return telemetryService.getTelemetryByDeviceId(device.getId(), hours, field);
    }
}