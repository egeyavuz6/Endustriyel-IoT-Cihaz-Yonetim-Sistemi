package com.argela.iot_device_management.dto;

import lombok.Data;

@Data
public class BulkCommandRequest {
    private String location;
    private String commandType;
}