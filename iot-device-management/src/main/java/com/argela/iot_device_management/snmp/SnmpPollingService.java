package com.argela.iot_device_management.snmp;

import com.argela.iot_device_management.snmp.entity.DeviceDataEntity;
import com.argela.iot_device_management.snmp.entity.SimulatorEntity;
import com.argela.iot_device_management.snmp.repository.DeviceDataEntityRepository;
import com.argela.iot_device_management.snmp.repository.SimulatorEntityRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SnmpPollingService {

    private final SimulatorEntityRepository simulatorEntityRepository;
    private final DeviceDataEntityRepository deviceDataEntityRepository;
    private final SnmpService snmpService;
    private final DockerDiscoveryService dockerDiscoveryService;

    public SnmpPollingService(SimulatorEntityRepository simulatorEntityRepository,
                              DeviceDataEntityRepository deviceDataEntityRepository,
                              SnmpService snmpService,
                              DockerDiscoveryService dockerDiscoveryService) {
        this.simulatorEntityRepository = simulatorEntityRepository;
        this.deviceDataEntityRepository = deviceDataEntityRepository;
        this.snmpService = snmpService;
        this.dockerDiscoveryService = dockerDiscoveryService;
    }

    @Scheduled(fixedRate = 5000)
    public void pollAllDevices() {
        List<SimulatorEntity> devices = simulatorEntityRepository.findAll();

        for (SimulatorEntity device : devices) {
            pollSingleDevice(device);
        }
    }

    private void pollSingleDevice(SimulatorEntity device) {
        Optional<Integer> hostPort = dockerDiscoveryService.findHostPortByInternalIp(device.getIpAddress(), 1161);

        if (hostPort.isEmpty()) {
            System.out.println("Cihaz " + device.getId() + " icin port bulunamadi, atlaniyor.");
            return;
        }

        List<DeviceDataEntity> dataPoints = deviceDataEntityRepository.findBySimulatorId(device.getId());

        List<String> oids = dataPoints.stream()
                .map(DeviceDataEntity::getOid)
                .toList();

        if (oids.isEmpty()) {
            return;
        }

        snmpService.getMultipleOidsAsync("127.0.0.1", hostPort.get(), "public", oids, results -> {
            System.out.println("Cihaz " + device.getId() + " (port " + hostPort.get() + ") sonuclari: " + results);
        });
    }
}