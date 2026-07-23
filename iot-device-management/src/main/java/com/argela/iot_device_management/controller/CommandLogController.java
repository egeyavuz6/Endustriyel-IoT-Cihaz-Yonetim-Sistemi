package com.argela.iot_device_management.controller;

import com.argela.iot_device_management.dto.CreateCommandLogRequest;
import com.argela.iot_device_management.entity.CommandLog;
import com.argela.iot_device_management.service.CommandLogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/command-logs")
public class CommandLogController {

    private final CommandLogService commandLogService;

    public CommandLogController(CommandLogService commandLogService) {
        this.commandLogService = commandLogService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PostMapping
    public ResponseEntity<CommandLog> createCommandLog(@RequestBody CreateCommandLogRequest request) {
        CommandLog log = commandLogService.createCommandLog(
                request.getDeviceId(),
                request.getCommandId(),
                request.getExecutedBy()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(log);
    }

    @GetMapping("/device/{deviceId}")
    public List<CommandLog> getCommandLogsByDevice(@PathVariable Long deviceId) {
        return commandLogService.getCommandLogsByDeviceId(deviceId);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PutMapping("/{logId}/execute")
    public ResponseEntity<CommandLog> markExecuted(@PathVariable Long logId) {
        CommandLog log = commandLogService.markAsExecuted(logId);
        return ResponseEntity.ok(log);
    }
}