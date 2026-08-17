package com.argela.iot_device_management.repository;

import com.argela.iot_device_management.entity.DeviceTypeTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceTypeTemplateRepository extends JpaRepository<DeviceTypeTemplate, Long> {
    List<DeviceTypeTemplate> findByDeviceType(String deviceType);
}