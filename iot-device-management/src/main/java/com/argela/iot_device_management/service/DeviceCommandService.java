package com.argela.iot_device_management.service;

import com.argela.iot_device_management.dto.TelemetrySettingsRequest;
import com.argela.iot_device_management.dto.UpdateCommandTypeRequest;
import com.argela.iot_device_management.entity.Device;
import com.argela.iot_device_management.entity.DeviceCommand;
import com.argela.iot_device_management.exception.ResourceNotFoundException;
import com.argela.iot_device_management.repository.DeviceCommandRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import com.argela.iot_device_management.dto.CreateCommandTypeRequest;


import java.util.List;

@Service
public class DeviceCommandService {

    private final DeviceCommandRepository deviceCommandRepository;
    private final DeviceService deviceService;


    public DeviceCommandService(DeviceCommandRepository deviceCommandRepository, DeviceService deviceService) {
        this.deviceCommandRepository = deviceCommandRepository;
        this.deviceService = deviceService;
    }

    public List<DeviceCommand> getAllCommands() {
        return deviceCommandRepository.findAll();
    }

    public DeviceCommand getCommandById(Long id) {
        return deviceCommandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Command not found with id: " + id));
    }

    public DeviceCommand createCommandType(CreateCommandTypeRequest request) {
        Device device = deviceService.getDeviceById(request.getDeviceId());

        DeviceCommand command = new DeviceCommand();
        command.setDevice(device);
        command.setCommandType(request.getCommandType());
        command.setOperationType(request.getOperationType());
        command.setMinValue(request.getMinValue());
        command.setMaxValue(request.getMaxValue());
        command.setIsActive(request.getIsActive() != null ? request.getIsActive() : false);
        command.setCreatedAt(LocalDateTime.now());

        return deviceCommandRepository.save(command);
    }
    public DeviceCommand updateCommandType(Long id, UpdateCommandTypeRequest request) {
        DeviceCommand command = getCommandById(id);

        if ("READ".equals(command.getOperationType())) {
            throw new IllegalArgumentException("READ tipi komutlar API uzerinden guncellenemez, sadece veritabanindan elle degistirilebilir.");
        }
        if (request.getMinValue() != null) {
            command.setMinValue(request.getMinValue());
        }
        if (request.getMaxValue() != null) {
            command.setMaxValue(request.getMaxValue());
        }
        if (request.getThresholdValue() != null) {
            command.setThresholdValue(request.getThresholdValue());
        }
        if (request.getAlarmEnabled() != null) {
            command.setAlarmEnabled(request.getAlarmEnabled());
        }
        if (request.getIsActive() != null) {
            command.setIsActive(request.getIsActive());
        }

        return deviceCommandRepository.save(command);
    }
    public List<DeviceCommand> getActiveAlarms() {
        return deviceCommandRepository.findAll().stream()
                .filter(c -> "ACTIVE".equals(c.getAlarmState()))
                .toList();
    }

    public List<DeviceCommand> getCommandsByDeviceId(Long deviceId) {
        return deviceCommandRepository.findByDeviceId(deviceId);
    }

    public DeviceCommand updateTelemetrySettings(Long id, TelemetrySettingsRequest request) {
        DeviceCommand command = getCommandById(id);

        if (!"READ".equals(command.getOperationType())) {
            throw new IllegalArgumentException("Telemetri ayarlari sadece READ tipi komutlar icin gecerlidir.");
        }

        if (request.getThresholdValue() != null) {
            command.setThresholdValue(request.getThresholdValue());
        }
        if (request.getAlarmEnabled() != null) {
            command.setAlarmEnabled(request.getAlarmEnabled());
        }
        if (request.getIsActive() != null) {
            command.setIsActive(request.getIsActive());
        }

        return deviceCommandRepository.save(command);
    }

}