package com.argela.iot_device_management.service;

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

    public DeviceCommandService(DeviceCommandRepository deviceCommandRepository) {
        this.deviceCommandRepository = deviceCommandRepository;
    }

    public List<DeviceCommand> getAllCommands() {
        return deviceCommandRepository.findAll();
    }

    public DeviceCommand getCommandById(Long id) {
        return deviceCommandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Command not found with id: " + id));
    }

    public DeviceCommand createCommandType(CreateCommandTypeRequest request) {
        boolean exists;
        if (deviceCommandRepository.findAll().stream()
                .anyMatch(c -> c.getCommandType().equalsIgnoreCase(request.getCommandType()))) exists = true;
        else exists = false;

        if (exists) {
            throw new IllegalArgumentException("Bu komut tipi zaten mevcut: " + request.getCommandType());
        }

        DeviceCommand command = new DeviceCommand();
        command.setCommandType(request.getCommandType());
        command.setOperationType(request.getOperationType());
        command.setMinValue(request.getMinValue());
        command.setMaxValue(request.getMaxValue());
        command.setCreatedAt(LocalDateTime.now());

        return deviceCommandRepository.save(command);
    }
}