package com.argela.iot_device_management.service;

import com.argela.iot_device_management.entity.DeviceCommand;
import com.argela.iot_device_management.exception.ResourceNotFoundException;
import com.argela.iot_device_management.repository.DeviceCommandRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public List<DeviceCommand> getCommandsByDeviceId(Long deviceId) {
        return deviceCommandRepository.findByDeviceId(deviceId);
    }

    public DeviceCommand createCommand(DeviceCommand command) {
        command.setStatus("PENDING");
        command.setCreatedAt(LocalDateTime.now());
        return deviceCommandRepository.save(command);
    }

    public DeviceCommand updateCommand(Long id, DeviceCommand updatedCommand) {
        DeviceCommand command = getCommandById(id);
        command.setCommandType(updatedCommand.getCommandType());
        command.setCommandValue(updatedCommand.getCommandValue());
        command.setStatus(updatedCommand.getStatus());
        return deviceCommandRepository.save(command);
    }

    public void deleteCommand(Long id) {
        deviceCommandRepository.deleteById(id);
    }
}