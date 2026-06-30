package com.argela.iot_device_management.service;

import com.argela.iot_device_management.entity.Device;
import com.argela.iot_device_management.repository.DeviceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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
                .orElseThrow(() -> new RuntimeException("Device not found with id: " + id));
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
}