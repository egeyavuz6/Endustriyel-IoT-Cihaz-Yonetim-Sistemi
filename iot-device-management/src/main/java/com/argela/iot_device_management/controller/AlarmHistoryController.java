package com.argela.iot_device_management.controller;

import com.argela.iot_device_management.entity.AlarmHistory;
import com.argela.iot_device_management.repository.AlarmHistoryRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/alarm-history")
public class AlarmHistoryController {

    private final AlarmHistoryRepository alarmHistoryRepository;

    public AlarmHistoryController(AlarmHistoryRepository alarmHistoryRepository) {
        this.alarmHistoryRepository = alarmHistoryRepository;
    }

    @GetMapping
    public List<AlarmHistory> getAlarmHistory(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate to,
            @RequestParam(required = false) Long deviceId) {

        OffsetDateTime effectiveTo = (to != null)
                ? to.atStartOfDay(ZoneId.systemDefault()).plusDays(1).toOffsetDateTime()
                : OffsetDateTime.now();

        OffsetDateTime effectiveFrom;
        if (from != null) {
            effectiveFrom = from.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
        } else {
            effectiveFrom = effectiveTo.minusMonths(3);
        }

        return alarmHistoryRepository.findByDateRangeAndOptionalDevice(effectiveFrom, effectiveTo, deviceId);
    }
}