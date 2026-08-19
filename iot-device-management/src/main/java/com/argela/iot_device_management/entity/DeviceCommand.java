package com.argela.iot_device_management.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "device_commands")
@Data
public class DeviceCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Column(name = "command_type", nullable = false)
    private String commandType;

    @Column(name = "operation_type")
    private String operationType;

    @Column(name = "min_value")
    private Double minValue;

    @Column(name = "max_value")
    private Double maxValue;

    @Column(name = "data_type")
    private String dataType;

    @Column(name = "alarm_state")
    private String alarmState;

    @Column(name = "alarm_enabled")
    private Boolean alarmEnabled;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "alarm_check_type")
    private String alarmCheckType;

    @Column(name = "alarm_min_threshold")
    private String alarmMinThreshold;

    @Column(name = "alarm_max_threshold")
    private String alarmMaxThreshold;

    @Column(name = "possible_values")
    private String possibleValues;
}