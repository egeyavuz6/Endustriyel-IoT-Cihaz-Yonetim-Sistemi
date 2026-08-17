package com.argela.iot_device_management.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "device_type_templates")
@Data
public class DeviceTypeTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_type", nullable = false)
    private String deviceType;

    @Column(name = "command_type", nullable = false)
    private String commandType;

    @Column(name = "operation_type", nullable = false)
    private String operationType;

    @Column(name = "min_value")
    private Double minValue;

    @Column(name = "max_value")
    private Double maxValue;

    @Column(name = "data_type")
    private String dataType;

    @Column(name = "is_active_default")
    private Boolean isActiveDefault;
}