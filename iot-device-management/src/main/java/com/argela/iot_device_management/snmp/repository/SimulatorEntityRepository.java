package com.argela.iot_device_management.snmp.repository;

import com.argela.iot_device_management.snmp.entity.SimulatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SimulatorEntityRepository extends JpaRepository<SimulatorEntity, Long> {

    @Query("SELECT DISTINCT s FROM SimulatorEntity s LEFT JOIN FETCH s.dataPoints")
    List<SimulatorEntity> findAllWithDataPoints();
}