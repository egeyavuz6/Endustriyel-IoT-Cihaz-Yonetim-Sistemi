package com.argela.iot_device_management.controller;

import com.argela.iot_device_management.entity.CommandLog;
import com.argela.iot_device_management.entity.Device;
import com.argela.iot_device_management.entity.DeviceCommand;
import com.argela.iot_device_management.service.CommandLogService;
import com.argela.iot_device_management.service.DeviceService;
import com.argela.iot_device_management.service.DeviceCommandService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;
    private final CommandLogService commandLogService;

    public DeviceController(DeviceService deviceService, CommandLogService commandLogService) {
        this.deviceService = deviceService;
        this.commandLogService = commandLogService;
    }

    @GetMapping
    public List<Device> getAllDevices() {
        return deviceService.getAllDevices();
    }
    
    @GetMapping("/with-status")
    public List<Map<String, Object>> getAllDevicesWithStatus() {
        return deviceService.getAllDevicesWithStatus();
    }
    @GetMapping("/{id}")
    public Device getDeviceById(@PathVariable Long id) {
        return deviceService.getDeviceById(id);
    }

    @GetMapping("/{id}/commands")
    public List<CommandLog> getDeviceCommands(@PathVariable Long id) {
        return commandLogService.getCommandLogsByDeviceId(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Device> createDevice(@RequestBody Device device) {
        Device createdDevice = deviceService.createDevice(device);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDevice);//Oluşturulduğunda sadece 200 OK dönüyordu artık 201 Created dönecek.
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Device updateDevice(@PathVariable Long id, @RequestBody Device device) {
        return deviceService.updateDevice(id, device);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
    }

    @GetMapping("/location-summary")
    public Map<String, Map<String, Object>> getLocationSummary() {
        return deviceService.getDevicesByLocation();
    }


}