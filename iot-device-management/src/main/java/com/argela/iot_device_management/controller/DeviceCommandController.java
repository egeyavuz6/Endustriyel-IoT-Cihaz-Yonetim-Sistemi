package com.argela.iot_device_management.controller;

import com.argela.iot_device_management.entity.DeviceCommand;
import com.argela.iot_device_management.service.DeviceCommandService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/commands")
public class DeviceCommandController {

    private final DeviceCommandService deviceCommandService;

    public DeviceCommandController(DeviceCommandService deviceCommandService) {
        this.deviceCommandService = deviceCommandService;
    }

    @GetMapping
    public List<DeviceCommand> getAllCommands() {
        return deviceCommandService.getAllCommands();
    }

    @GetMapping("/{id}")
    public DeviceCommand getCommandById(@PathVariable Long id) {
        return deviceCommandService.getCommandById(id);
    }
}