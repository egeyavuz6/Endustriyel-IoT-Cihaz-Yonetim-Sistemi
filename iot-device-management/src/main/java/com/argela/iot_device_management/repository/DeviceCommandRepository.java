package com.argela.iot_device_management.repository;

import com.argela.iot_device_management.entity.DeviceCommand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceCommandRepository extends JpaRepository<DeviceCommand, Long> {
    Optional<DeviceCommand> findByDeviceIdAndCommandTypeAndOperationType(Long deviceId, String commandType, String operationType);
    List<DeviceCommand> findByDeviceId(Long deviceId);
}