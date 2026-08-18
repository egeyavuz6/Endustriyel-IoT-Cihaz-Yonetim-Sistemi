package com.argela.iot_device_management.repository;

import com.argela.iot_device_management.entity.AlarmHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface AlarmHistoryRepository extends JpaRepository<AlarmHistory, Long> {
    @Query("SELECT a FROM AlarmHistory a " +
            "WHERE a.createdAt BETWEEN :from AND :to " +
            "AND (:deviceId IS NULL OR a.device.id = :deviceId) " +
            "ORDER BY a.createdAt DESC")
    List<AlarmHistory> findByDateRangeAndOptionalDevice(
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to,
            @Param("deviceId") Long deviceId
    );
}