package com.argela.iot_device_management.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TelemetryRequest {

    @NotNull(message = "deviceId zorunludur")
    private Long deviceId;

    @NotNull(message = "temperature zorunludur")
    private Double temperature;

    @NotNull(message = "humidity zorunludur")
    private Double humidity;

    @NotNull(message = "pressure zorunludur")
    private Double pressure;

    @NotNull(message = "vibration zorunludur")
    private Double vibration;
}