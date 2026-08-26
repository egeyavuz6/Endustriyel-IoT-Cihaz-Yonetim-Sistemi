package com.argela.iot_device_management.snmp.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "device_data_entity")
@Data
public class DeviceDataEntity {

    @Id
    private Long id;

    @Column(name = "value_name")
    private String valueName;

    private String oid;

    @Column(name = "return_type")
    private String returnType;

    @Column(name = "return_value")
    private String returnValue;

    @ManyToOne
    @JoinColumn(name = "simulator_id")
    private SimulatorEntity simulator;
}