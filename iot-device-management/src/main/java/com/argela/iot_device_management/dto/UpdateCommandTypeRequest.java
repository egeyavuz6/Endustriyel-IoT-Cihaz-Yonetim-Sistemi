package com.argela.iot_device_management.dto;

import lombok.Data;

@Data
public class UpdateCommandTypeRequest {
    private Double minValue;
    private Double maxValue;
    private Double thresholdValue;
    private Boolean alarmEnabled;
    private Boolean isActive;
}