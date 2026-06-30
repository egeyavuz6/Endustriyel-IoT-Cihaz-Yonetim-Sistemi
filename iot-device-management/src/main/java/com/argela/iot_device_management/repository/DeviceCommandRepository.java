package com.argela.iot_device_management.repository;

import com.argela.iot_device_management.entity.DeviceCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceCommandRepository extends JpaRepository<DeviceCommand, Long> {
}