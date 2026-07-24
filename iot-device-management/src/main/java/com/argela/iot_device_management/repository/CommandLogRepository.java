package com.argela.iot_device_management.repository;

import com.argela.iot_device_management.entity.CommandLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommandLogRepository extends JpaRepository<CommandLog, Long> {
    List<CommandLog> findByDeviceId(Long deviceId);
    List<CommandLog> findTop10ByOrderByCreatedAtDesc();
}