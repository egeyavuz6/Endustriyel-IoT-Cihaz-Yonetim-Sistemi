package com.argela.iot_device_management.snmp;

import com.argela.iot_device_management.snmp.entity.DeviceDataEntity;
import com.argela.iot_device_management.snmp.entity.SimulatorEntity;
import com.argela.iot_device_management.snmp.repository.SimulatorEntityRepository;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class SnmpPollingService {

    private static final Logger log = LoggerFactory.getLogger(SnmpPollingService.class);

    private final SimulatorEntityRepository simulatorEntityRepository;
    private final SnmpService snmpService;
    private final DockerDiscoveryService dockerDiscoveryService;
    private final WriteApi writeApi;

    @Value("${influxdb.bucket}")
    private String bucket;

    @Value("${influxdb.org}")
    private String org;

    @Value("${snmp.default.port:1161}")
    private int containerPort;

    @Value("${snmp.default.community:public}")
    private String defaultCommunity;

    @Value("${snmp.default.target-ip:127.0.0.1}")
    private String targetIp;

    public SnmpPollingService(SimulatorEntityRepository simulatorEntityRepository,
                              SnmpService snmpService,
                              DockerDiscoveryService dockerDiscoveryService,
                              WriteApi writeApi) {
        this.simulatorEntityRepository = simulatorEntityRepository;
        this.snmpService = snmpService;
        this.dockerDiscoveryService = dockerDiscoveryService;
        this.writeApi = writeApi;
    }

    @Scheduled(fixedRateString = "${snmp.polling.rate-ms:5000}")
    public void pollAllDevices() {
        List<SimulatorEntity> devices = simulatorEntityRepository.findAllWithDataPoints();

        for (SimulatorEntity device : devices) {
            pollSingleDevice(device);
        }
    }

    private void pollSingleDevice(SimulatorEntity device) {
        dockerDiscoveryService.findHostPortByInternalIp(device.getIpAddress(), containerPort)
                .ifPresentOrElse(
                        hostPort -> executeSnmpPoll(device, hostPort),
                        () -> log.warn("Cihaz için Docker portu bulunamadı. IP: {}", device.getIpAddress())
                );
    }

    private void executeSnmpPoll(SimulatorEntity device, int hostPort) {
        List<DeviceDataEntity> dataPoints = device.getDataPoints();
        if (dataPoints == null || dataPoints.isEmpty()) {
            return;
        }

        List<String> oids = dataPoints.stream()
                .map(DeviceDataEntity::getOid)
                .toList();

        snmpService.getMultipleOidsAsync(targetIp, hostPort, defaultCommunity, oids, results -> {
            if (!results.isEmpty()) {
                writeToInflux(device.getId(), dataPoints, results);
            } else {
                log.warn("Cihazdan boş yanıt döndü veya zaman aşımı. Device ID: {}", device.getId());
            }
        });
    }

    private void writeToInflux(Long deviceId, List<DeviceDataEntity> dataPoints, Map<String, String> results) {
        Point point = Point.measurement("snmp_telemetry")
                .addTag("device_id", deviceId.toString())
                .time(Instant.now(), WritePrecision.MS);

        boolean hasValidField = false;

        for (DeviceDataEntity dataPoint : dataPoints) {
            String rawValue = results.get(dataPoint.getOid());
            if (rawValue == null) continue;

            if (addFieldByType(point, dataPoint.getValueName(), dataPoint.getReturnType(), rawValue)) {
                hasValidField = true;
            }
        }

        if (hasValidField) {
            writeApi.writePoint(bucket, org, point);
        }
    }

    private boolean addFieldByType(Point point, String fieldName, String returnType, String rawValue) {
        try {
            switch (returnType.toUpperCase()) {
                case "INTEGER", "INT" -> point.addField(fieldName, Long.parseLong(rawValue));
                case "FLOAT", "DOUBLE" -> point.addField(fieldName, Double.parseDouble(rawValue));
                default -> point.addField(fieldName, rawValue);
            }
            return true;
        } catch (NumberFormatException e) {
            log.error("Veri tipi dönüştürme hatası. Field: {}, Value: {}, Type: {}", fieldName, rawValue, returnType);
            return false;
        }
    }
}