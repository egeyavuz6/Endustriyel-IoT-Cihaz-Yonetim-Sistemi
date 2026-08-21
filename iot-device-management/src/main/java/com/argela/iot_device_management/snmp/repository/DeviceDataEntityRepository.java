package com.argela.iot_device_management.snmp.repository;

import com.argela.iot_device_management.snmp.entity.DeviceDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceDataEntityRepository extends JpaRepository<DeviceDataEntity, Long> {
    List<DeviceDataEntity> findBySimulatorId(Long simulatorId);
}