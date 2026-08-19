package com.argela.iot_device_management.service;

import com.argela.iot_device_management.dto.CreateCommandTypeRequest;
import com.argela.iot_device_management.dto.TelemetrySettingsRequest;
import com.argela.iot_device_management.dto.UpdateCommandTypeRequest;
import com.argela.iot_device_management.entity.Device;
import com.argela.iot_device_management.entity.DeviceCommand;
import com.argela.iot_device_management.exception.ResourceNotFoundException;
import com.argela.iot_device_management.repository.DeviceCommandRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DeviceCommandService {

    private static final List<String> VALID_CHECK_TYPES = List.of("OUT_OF_RANGE", "IN_RANGE");

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

        boolean isString = "STRING".equals(request.getDataType());

        if (isString && (request.getMinValue() != null || request.getMaxValue() != null)) {
            throw new IllegalArgumentException("STRING tipi komutlar icin minValue/maxValue girilemez, bunun yerine possibleValues kullanin.");
        }

        if (!isString && request.getPossibleValues() != null) {
            throw new IllegalArgumentException("possibleValues sadece STRING tipi komutlar icin gecerlidir.");
        }

        DeviceCommand command = new DeviceCommand();
        command.setDevice(device);
        command.setCommandType(request.getCommandType());
        command.setOperationType(request.getOperationType());
        command.setMinValue(request.getMinValue());
        command.setMaxValue(request.getMaxValue());
        command.setDataType(request.getDataType());
        command.setPossibleValues(request.getPossibleValues());
        command.setIsActive(request.getIsActive() != null ? request.getIsActive() : false);
        command.setCreatedAt(LocalDateTime.now());

        return deviceCommandRepository.save(command);
    }

    public DeviceCommand updateCommandType(Long id, UpdateCommandTypeRequest request) {
        DeviceCommand command = getCommandById(id);

        if ("READ".equals(command.getOperationType())) {
            throw new IllegalArgumentException("READ tipi komutlar bu endpoint uzerinden guncellenemez.");
        }
        if ("R/W".equals(command.getOperationType())) {
            throw new IllegalArgumentException("R/W tipi komutlar bu endpoint uzerinden guncellenemez.");
        }
        if (request.getMinValue() != null) {
            command.setMinValue(request.getMinValue());
        }
        if (request.getMaxValue() != null) {
            command.setMaxValue(request.getMaxValue());
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

        if (request.getAlarmCheckType() != null) {
            if (!VALID_CHECK_TYPES.contains(request.getAlarmCheckType())) {
                throw new IllegalArgumentException(
                        "Gecersiz alarm_check_type. Lutfen su 2 secenekten birini girin: " +
                                String.join(", ", VALID_CHECK_TYPES)
                );
            }
            command.setAlarmCheckType(request.getAlarmCheckType());
        }
        if (request.getPossibleValues() != null) {
            if (!"STRING".equals(command.getDataType())){
                throw new IllegalArgumentException("STRING data tipine sahip verilere sadece possible values girisi yapilabilir.");
            }
        }
        if (request.getAlarmEnabled() != null) {
            command.setAlarmEnabled(request.getAlarmEnabled());
        }
        if (request.getIsActive() != null) {
            command.setIsActive(request.getIsActive());
        }
        if (request.getAlarmMinThreshold() != null) {
            command.setAlarmMinThreshold(request.getAlarmMinThreshold());
        }
        if (request.getAlarmMaxThreshold() != null) {
            command.setAlarmMaxThreshold(request.getAlarmMaxThreshold());
        }
        if (request.getPossibleValues() != null) {
            command.setPossibleValues(request.getPossibleValues());
        }

        return deviceCommandRepository.save(command);
    }
}