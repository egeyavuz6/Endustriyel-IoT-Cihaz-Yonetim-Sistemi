package com.argela.iot_device_management.service;

import com.argela.iot_device_management.entity.CommandLog;
import com.argela.iot_device_management.entity.Device;
import com.argela.iot_device_management.entity.DeviceCommand;
import com.argela.iot_device_management.exception.ResourceNotFoundException;
import com.argela.iot_device_management.repository.CommandLogRepository;
import com.argela.iot_device_management.repository.DeviceCommandRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommandLogService {

    private final CommandLogRepository commandLogRepository;
    private final DeviceCommandRepository deviceCommandRepository;
    private final DeviceService deviceService;

    public CommandLogService(CommandLogRepository commandLogRepository,
                             DeviceCommandRepository deviceCommandRepository,
                             DeviceService deviceService) {
        this.commandLogRepository = commandLogRepository;
        this.deviceCommandRepository = deviceCommandRepository;
        this.deviceService = deviceService;
    }

    public CommandLog createCommandLog(Long deviceId, Long commandId, String executedBy) {
        Device device = deviceService.getDeviceById(deviceId);

        DeviceCommand command = deviceCommandRepository.findById(commandId)
                .orElseThrow(() -> new ResourceNotFoundException("Command not found with id: " + commandId));

        CommandLog log = new CommandLog();
        log.setDevice(device);
        log.setCommand(command);
        log.setExecutedBy(executedBy);
        log.setStatus("PENDING");
        log.setCreatedAt(LocalDateTime.now());

        return commandLogRepository.save(log);
    }

    public List<CommandLog> getCommandLogsByDeviceId(Long deviceId) {
        return commandLogRepository.findByDeviceId(deviceId);
    }

    public CommandLog markAsExecuted(Long logId) {
        CommandLog log = commandLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Command log not found with id: " + logId));

        log.setStatus("EXECUTED");
        log.setExecutedAt(LocalDateTime.now());

        return commandLogRepository.save(log);
    }
}