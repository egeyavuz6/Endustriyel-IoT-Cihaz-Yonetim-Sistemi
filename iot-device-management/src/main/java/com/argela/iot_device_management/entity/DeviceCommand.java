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

    @Column(name = "command_type", nullable = false)
    private String commandType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "operation_type")
    private String operationType;


}