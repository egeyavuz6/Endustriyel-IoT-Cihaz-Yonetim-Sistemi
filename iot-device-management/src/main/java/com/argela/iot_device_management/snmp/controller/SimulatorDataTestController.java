package com.argela.iot_device_management.snmp.controller;

import com.argela.iot_device_management.snmp.entity.DeviceDataEntity;
import com.argela.iot_device_management.snmp.entity.SimulatorEntity;
import com.argela.iot_device_management.snmp.repository.DeviceDataEntityRepository;
import com.argela.iot_device_management.snmp.repository.SimulatorEntityRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/snmp/simulator-data")
public class SimulatorDataTestController {

    private final SimulatorEntityRepository simulatorEntityRepository;
    private final DeviceDataEntityRepository deviceDataEntityRepository;

    public SimulatorDataTestController(SimulatorEntityRepository simulatorEntityRepository,
                                       DeviceDataEntityRepository deviceDataEntityRepository) {
        this.simulatorEntityRepository = simulatorEntityRepository;
        this.deviceDataEntityRepository = deviceDataEntityRepository;
    }

    @GetMapping("/devices")
    public List<SimulatorEntity> getAllSimulatedDevices() {
        return simulatorEntityRepository.findAll();
    }

    @GetMapping("/devices/{id}/data")
    public List<DeviceDataEntity> getDeviceData(@PathVariable Long id) {
        return deviceDataEntityRepository.findBySimulatorId(id);
    }
}
