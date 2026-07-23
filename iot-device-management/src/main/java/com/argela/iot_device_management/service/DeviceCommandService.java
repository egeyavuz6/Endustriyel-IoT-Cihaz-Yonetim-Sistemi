package com.argela.iot_device_management.service;

import com.argela.iot_device_management.entity.DeviceCommand;
import com.argela.iot_device_management.exception.ResourceNotFoundException;
import com.argela.iot_device_management.repository.DeviceCommandRepository;
import org.springframework.stereotype.Service;

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
}