package com.argela.iot_device_management.controller;

import com.argela.iot_device_management.entity.Device;
import com.argela.iot_device_management.entity.DeviceCommand;
import com.argela.iot_device_management.service.DeviceService;
import com.argela.iot_device_management.service.DeviceCommandService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;
    private final DeviceCommandService deviceCommandService;

    public DeviceController(DeviceService deviceService, DeviceCommandService deviceCommandService) {
        this.deviceService = deviceService;
        this.deviceCommandService = deviceCommandService;
    }

    @GetMapping
    public List<Device> getAllDevices() {
        return deviceService.getAllDevices();
    }

    @GetMapping("/{id}")
    public Device getDeviceById(@PathVariable Long id) {
        return deviceService.getDeviceById(id);
    }

    @GetMapping("/{id}/commands")
    public List<DeviceCommand> getDeviceCommands(@PathVariable Long id) {
        return deviceCommandService.getCommandsByDeviceId(id);
    }

    @PostMapping
    public Device createDevice(@RequestBody Device device) {
        return deviceService.createDevice(device);
    }

    @PutMapping("/{id}")
    public Device updateDevice(@PathVariable Long id, @RequestBody Device device) {
        return deviceService.updateDevice(id, device);
    }

    @DeleteMapping("/{id}")
    public void deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
    }
}