package com.argela.iot_device_management.controller;

import com.argela.iot_device_management.dto.TelemetryRequest;
import com.argela.iot_device_management.entity.Device;
import com.argela.iot_device_management.service.DeviceService;
import com.argela.iot_device_management.service.TelemetryService;
import com.argela.iot_device_management.enums.TelemetryField;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;


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


    @Operation(
            summary="Seri numarasi ile telemetri sorgula."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Telemetri verisi başarıyla döndürüldü"),
            @ApiResponse(responseCode = "400", description = "Geçersiz fieldId değeri gönderildi"),
            @ApiResponse(responseCode = "401", description = "Kimlik doğrulama gerekli, token eksik veya geçersiz"),
            @ApiResponse(responseCode = "404", description = "Belirtilen seri numarasına sahip cihaz bulunamadı")
    })
    @GetMapping("/api/devices/serial/{serialNumber}/telemetry")
    public List<Map<String, Object>> getTelemetryBySerialNumber(
            @PathVariable String serialNumber,

            @RequestParam(defaultValue = "24") int hours,

            @Parameter(description = "1=temperature, 2=humidity, 3=pressure, 4=vibration, null=all fields")
            @RequestParam(required = false) Integer fieldId) {

        Device device = deviceService.getDeviceBySerialNumber(serialNumber);
        String fieldName = (fieldId != null) ? TelemetryField.fromId(fieldId).getFieldName() : null;
        return telemetryService.getTelemetryByDeviceId(device.getId(), hours, fieldName);
    }

}