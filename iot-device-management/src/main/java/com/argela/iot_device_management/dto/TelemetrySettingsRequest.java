package com.argela.iot_device_management.dto;

import lombok.Data;

@Data
public class TelemetrySettingsRequest {
    private Boolean alarmEnabled;
    private Boolean isActive;
    private String alarmCheckType;
    private String alarmMinThreshold;
    private String alarmMaxThreshold;
    private String possibleValues;
}