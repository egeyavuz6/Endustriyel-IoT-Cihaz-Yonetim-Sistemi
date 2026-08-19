package com.argela.iot_device_management.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Table(name = "alarm_history")
@Data
public class AlarmHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @ManyToOne
    @JoinColumn(name = "command_id", nullable = false)
    private DeviceCommand command;

    private String value;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "clear_value")
    private String clearValue;

    @Column(name = "cleared_at")
    private OffsetDateTime clearedAt;
}