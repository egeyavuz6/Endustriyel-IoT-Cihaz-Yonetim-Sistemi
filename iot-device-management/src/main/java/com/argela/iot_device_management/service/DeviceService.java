package com.argela.iot_device_management.service;
import com.argela.iot_device_management.entity.DeviceCommand;
import com.argela.iot_device_management.entity.DeviceTypeTemplate;
import com.argela.iot_device_management.repository.DeviceCommandRepository;
import com.argela.iot_device_management.repository.DeviceTypeTemplateRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.argela.iot_device_management.entity.Device;
import com.argela.iot_device_management.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import com.argela.iot_device_management.exception.ResourceNotFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceCommandRepository deviceCommandRepository;
    private final DeviceTypeTemplateRepository deviceTypeTemplateRepository;

    public DeviceService(DeviceRepository deviceRepository,  DeviceCommandRepository deviceCommandRepository, DeviceTypeTemplateRepository deviceTypeTemplateRepository) {
        this.deviceRepository = deviceRepository;
        this.deviceCommandRepository = deviceCommandRepository;
        this.deviceTypeTemplateRepository = deviceTypeTemplateRepository;
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public Device getDeviceById(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));
    }
    public Device createDevice(Device device) {
        device.setCreatedAt(LocalDateTime.now());
        Device savedDevice = deviceRepository.save(device);

        List<DeviceTypeTemplate> templates = deviceTypeTemplateRepository.findByDeviceType(device.getType());

        for (DeviceTypeTemplate template : templates) {
            DeviceCommand command = new DeviceCommand();
            command.setDevice(savedDevice);
            command.setCommandType(template.getCommandType());
            command.setOperationType(template.getOperationType());
            command.setMinValue(template.getMinValue());
            command.setMaxValue(template.getMaxValue());
            command.setDataType(template.getDataType());
            command.setIsActive(template.getIsActiveDefault());
            command.setCreatedAt(LocalDateTime.now());
            deviceCommandRepository.save(command);
        }

        return savedDevice;
    }

    public Device updateDevice(Long id, Device updatedDevice) {
        Device device = getDeviceById(id);
        device.setName(updatedDevice.getName());
        device.setSerialNumber(updatedDevice.getSerialNumber());
        device.setType(updatedDevice.getType());
        device.setLocation(updatedDevice.getLocation());
        return deviceRepository.save(device);
    }
    public void deleteDevice(Long id) {
        deviceRepository.deleteById(id);
    }

    public Device getDeviceBySerialNumber(String serialNumber) {
        return deviceRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with serial number: " + serialNumber));
    }

    @PersistenceContext
    private EntityManager entityManager;

    public Map<String, Map<String, Object>> getDevicesByLocation() {
        List<Device> allDevices = deviceRepository.findAll();

        Map<String, List<Device>> grouped = allDevices.stream()
                .collect(Collectors.groupingBy(Device::getLocation));

        Map<String, Map<String, Object>> result = new HashMap<>();

        for (Map.Entry<String, List<Device>> entry : grouped.entrySet()) {
            String location = entry.getKey();
            List<Device> devices = entry.getValue();

            long activeCount = devices.stream()
                    .filter(d -> isDeviceActive(d.getId()))
                    .count();

            Map<String, Object> summary = new HashMap<>();
            summary.put("totalDevices", devices.size());
            summary.put("activeDevices", activeCount);
            summary.put("passiveDevices", devices.size() - activeCount);

            result.put(location, summary);
        }

        return result;
    }

    private boolean isDeviceActive(Long deviceId) {
        Object result = entityManager.createNativeQuery(
                "SELECT current_state FROM device_commands WHERE device_id = :deviceId AND command_type = 'POWER_ON' AND operation_type = 'READ'"
                )
                .setParameter("deviceId", deviceId)
                .getResultStream()
                .findFirst()
                .orElse(null);
        return "ON".equals(result);
    }

    public List<Map<String, Object>> getAllDevicesWithStatus() {
        List<Object[]> rows = entityManager.createNativeQuery(
                "SELECT d.id, d.name, d.serial_number, d.type, d.location, " +
                        "CASE WHEN dc.current_state = 'ON' THEN 'ACTIVE' ELSE 'PASSIVE' END as status " +
                        "FROM devices d " +
                        "LEFT JOIN device_commands dc ON dc.device_id = d.id " +
                        "AND dc.command_type = 'POWER_ON' AND dc.operation_type = 'R/W'"
        ).getResultList();

        return rows.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", row[0]);
            map.put("name", row[1]);
            map.put("serialNumber", row[2]);
            map.put("type", row[3]);
            map.put("location", row[4]);
            map.put("status", row[5]);
            return map;
        }).collect(Collectors.toList());
    }
    public List<Device> getDevicesByLocationName(String location) {
        return deviceRepository.findByLocation(location);
    }
}