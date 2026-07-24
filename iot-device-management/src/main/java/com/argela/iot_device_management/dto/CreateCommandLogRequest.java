package com.argela.iot_device_management.dto;

import lombok.Data;

@Data
public class CreateCommandLogRequest {
    private Long deviceId;
    private Long commandId;
    private String commandValue;
}