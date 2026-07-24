package com.argela.iot_device_management.service;

import com.argela.iot_device_management.dto.UpdateCommandTypeRequest;
import com.argela.iot_device_management.entity.DeviceCommand;
import com.argela.iot_device_management.exception.ResourceNotFoundException;
import com.argela.iot_device_management.repository.DeviceCommandRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import com.argela.iot_device_management.dto.CreateCommandTypeRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
public class DeviceCommandService {

    private final DeviceCommandRepository deviceCommandRepository;

    @PersistenceContext
    private EntityManager entityManager;

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
    public DeviceCommand updateCommandType(Long id, UpdateCommandTypeRequest request) {
        DeviceCommand command = getCommandById(id);

        if (request.getMinValue() != null) {
            command.setMinValue(request.getMinValue());
        }
        if (request.getMaxValue() != null) {
            command.setMaxValue(request.getMaxValue());
        }
        if (request.getThresholdValue() != null) {
            command.setThresholdValue(request.getThresholdValue());
        }
        if (request.getAlarmState() != null) {
            if (!request.getAlarmState().equals("ACTIVE") && !request.getAlarmState().equals("INACTIVE")) {
                throw new IllegalArgumentException("Gecersiz alarm state: " + request.getAlarmState());
            }
            command.setAlarmState(request.getAlarmState());
        }


        return deviceCommandRepository.save(command);
    }

    @Transactional
    public void assignCommandsToDeviceType(String deviceType, List<Long> commandIds) {
        for (Long commandId : commandIds) {
            getCommandById(commandId);

            entityManager.createNativeQuery(
                            "INSERT INTO device_type_commands (device_type, command_id) VALUES (?, ?) " +
                                    "ON CONFLICT DO NOTHING"
                    )
                    .setParameter(1, deviceType)
                    .setParameter(2, commandId)
                    .executeUpdate();
        }
    }
    public List<DeviceCommand> getActiveAlarms() {
        return deviceCommandRepository.findAll().stream()
                .filter(c -> "ACTIVE".equals(c.getAlarmState()))
                .toList();
    }

}