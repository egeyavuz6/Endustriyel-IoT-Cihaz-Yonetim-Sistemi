package com.argela.iot_device_management.controller;

import com.argela.iot_device_management.dto.AssignCommandToDeviceTypeRequest;
import com.argela.iot_device_management.dto.CreateCommandTypeRequest;
import com.argela.iot_device_management.dto.UpdateCommandTypeRequest;
import com.argela.iot_device_management.entity.DeviceCommand;
import com.argela.iot_device_management.service.DeviceCommandService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<DeviceCommand> createCommandType(@RequestBody CreateCommandTypeRequest request) {
        DeviceCommand created = deviceCommandService.createCommandType(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<DeviceCommand> updateCommandType(
            @PathVariable Long id,
            @RequestBody UpdateCommandTypeRequest request) {
        DeviceCommand updated = deviceCommandService.updateCommandType(id, request);
        return ResponseEntity.ok(updated);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/assign-to-device-type")
    public ResponseEntity<String> assignCommandsToDeviceType(@RequestBody AssignCommandToDeviceTypeRequest request) {
        deviceCommandService.assignCommandsToDeviceType(request.getDeviceType(), request.getCommandIds());
        return ResponseEntity.ok(request.getCommandIds().size() + " komut, " + request.getDeviceType() + " tipine atandi.");
    }
    @GetMapping("/alarms")
    public List<DeviceCommand> getActiveAlarms() {
        return deviceCommandService.getActiveAlarms();
    }
}