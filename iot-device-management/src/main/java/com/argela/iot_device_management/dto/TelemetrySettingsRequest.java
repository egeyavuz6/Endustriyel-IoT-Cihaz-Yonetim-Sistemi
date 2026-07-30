package com.argela.iot_device_management.dto;

import lombok.Data;

@Data
public class TelemetrySettingsRequest {
    private Double thresholdValue;
    private Boolean alarmEnabled;
    private Boolean isActive;
}