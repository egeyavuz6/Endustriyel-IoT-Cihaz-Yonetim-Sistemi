package com.argela.iot_device_management.dto;

import lombok.Data;

@Data
public class AlarmSettingsRequest {
    private Double thresholdValue;
    private Boolean alarmEnabled;
}