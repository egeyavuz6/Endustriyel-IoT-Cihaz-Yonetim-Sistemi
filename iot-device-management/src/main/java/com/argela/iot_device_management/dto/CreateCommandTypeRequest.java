package com.argela.iot_device_management.dto;

import lombok.Data;

@Data
public class CreateCommandTypeRequest {
    private Long deviceId;
    private String commandType;
    private String operationType;
    private Double minValue;
    private Double maxValue;
    private Boolean isActive;
}