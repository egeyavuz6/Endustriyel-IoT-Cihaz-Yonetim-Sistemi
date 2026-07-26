package com.argela.iot_device_management.dto;

import lombok.Data;

import java.util.List;

@Data
public class AssignCommandToDeviceTypeRequest {
    private String deviceType;
    private List<Long> commandIds;
}