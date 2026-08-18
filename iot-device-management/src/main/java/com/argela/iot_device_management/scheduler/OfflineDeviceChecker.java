package com.argela.iot_device_management.scheduler;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OfflineDeviceChecker {

    @PersistenceContext
    private EntityManager entityManager;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkOfflineDevices() {
        int affected = entityManager.createNativeQuery(
                "UPDATE devices d SET connection_status = 'OFFLINE' " +
                        "FROM device_types dt " +
                        "WHERE d.type = dt.device_type " +
                        "AND d.connection_status = 'ONLINE' " +
                        "AND d.last_seen_at < NOW() - (dt.offline_threshold_minutes || ' minutes')::INTERVAL"
        ).executeUpdate();

        entityManager.createNativeQuery(
                "UPDATE device_commands SET current_state = 'OFF' " +
                        "WHERE command_type = 'POWER_ON' AND operation_type = 'R/W' " +
                        "AND device_id IN (SELECT id FROM devices WHERE connection_status = 'OFFLINE')"
        ).executeUpdate();

        entityManager.createNativeQuery(
                "UPDATE device_commands SET current_state = 'STOPPED' " +
                        "WHERE command_type = 'START' AND operation_type = 'R/W' " +
                        "AND device_id IN (SELECT id FROM devices WHERE connection_status = 'OFFLINE')"
        ).executeUpdate();

        if (affected > 0) {
            System.out.println(affected + " cihaz OFFLINE olarak isaretlendi ve POWER_ON/START durumu sifirlandi.");
        }
    }
}