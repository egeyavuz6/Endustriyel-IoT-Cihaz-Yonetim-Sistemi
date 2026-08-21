package com.argela.iot_device_management.snmp;

import com.argela.iot_device_management.snmp.entity.DeviceDataEntity;
import com.argela.iot_device_management.snmp.entity.SimulatorEntity;
import com.argela.iot_device_management.snmp.repository.DeviceDataEntityRepository;
import com.argela.iot_device_management.snmp.repository.SimulatorEntityRepository;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SnmpPollingService {

    private final SimulatorEntityRepository simulatorEntityRepository;
    private final DeviceDataEntityRepository deviceDataEntityRepository;
    private final SnmpService snmpService;
    private final DockerDiscoveryService dockerDiscoveryService;
    private final WriteApi writeApi;

    @Value("${influxdb.bucket}")
    private String bucket;

    @Value("${influxdb.org}")
    private String org;

    public SnmpPollingService(SimulatorEntityRepository simulatorEntityRepository,
                              DeviceDataEntityRepository deviceDataEntityRepository,
                              SnmpService snmpService,
                              DockerDiscoveryService dockerDiscoveryService,
                              WriteApi writeApi) {
        this.simulatorEntityRepository = simulatorEntityRepository;
        this.deviceDataEntityRepository = deviceDataEntityRepository;
        this.snmpService = snmpService;
        this.dockerDiscoveryService = dockerDiscoveryService;
        this.writeApi = writeApi;
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
            writeToInflux(device.getId(), dataPoints, results);
        });
    }

    private void writeToInflux(Long deviceId, List<DeviceDataEntity> dataPoints, Map<String, String> results) {
        Point point = Point.measurement("snmp_telemetry")
                .addTag("device_id", deviceId.toString())
                .time(java.time.Instant.now(), WritePrecision.MS);

        for (DeviceDataEntity dataPoint : dataPoints) {
            String rawValue = results.get(dataPoint.getOid());
            if (rawValue == null) continue;

            addFieldByType(point, dataPoint.getValueName(), dataPoint.getReturnType(), rawValue);
        }

        writeApi.writePoint(bucket, org, point);
    }

    private void addFieldByType(Point point, String fieldName, String returnType, String rawValue) {
        try {
            switch (returnType) {
                case "INTEGER" -> point.addField(fieldName, Long.parseLong(rawValue));
                case "FLOAT" -> point.addField(fieldName, Double.parseDouble(rawValue));
                default -> point.addField(fieldName, rawValue);
            }
        } catch (NumberFormatException e) {
            point.addField(fieldName, rawValue);
        }
    }
}