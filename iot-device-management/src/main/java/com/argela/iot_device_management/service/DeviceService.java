package com.argela.iot_device_management.service;

import com.argela.iot_device_management.entity.Device;
import com.argela.iot_device_management.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import com.argela.iot_device_management.exception.ResourceNotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceService(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public List<Device> getAllDevices() {
        return deviceRepository.findAll();
    }

    public Device getDeviceById(Long id) {
        return deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with id: " + id));
    }
    public Device createDevice(Device device) {
        return deviceRepository.save(device);
    }

    public Device updateDevice(Long id, Device updatedDevice) {
        Device device = getDeviceById(id);
        device.setName(updatedDevice.getName());
        device.setSerialNumber(updatedDevice.getSerialNumber());
        device.setType(updatedDevice.getType());
        device.setLocation(updatedDevice.getLocation());
        device.setStatus(updatedDevice.getStatus());
        return deviceRepository.save(device);
    }
    public void deleteDevice(Long id) {
        deviceRepository.deleteById(id);
    }

    public Device getDeviceBySerialNumber(String serialNumber) {
        return deviceRepository.findBySerialNumber(serialNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Device not found with serial number: " + serialNumber));
    }

    public Map<String, Map<String, Object>> getDevicesByLocation() {
        List<Device> allDevices = deviceRepository.findAll();

        Map<String, List<Device>> grouped = allDevices.stream()
                .collect(Collectors.groupingBy(Device::getLocation));

        Map<String, Map<String, Object>> result = new HashMap<>();

        for (Map.Entry<String, List<Device>> entry : grouped.entrySet()) {
            String location = entry.getKey();
            List<Device> devices = entry.getValue();

            long activeCount = devices.stream().filter(d -> "ACTIVE".equals(d.getStatus())).count();

            Map<String, Object> summary = new HashMap<>();
            summary.put("totalDevices", devices.size());
            summary.put("activeDevices", activeCount);
            summary.put("passiveDevices", devices.size() - activeCount);

            result.put(location, summary);
        }

        return result;
    }
}