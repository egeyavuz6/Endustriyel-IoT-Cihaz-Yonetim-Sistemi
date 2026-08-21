package com.argela.iot_device_management.snmp.repository;

import com.argela.iot_device_management.snmp.entity.SimulatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimulatorEntityRepository extends JpaRepository<SimulatorEntity, Long> {
}