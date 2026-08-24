package com.argela.iot_device_management.snmp.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "simulator_entity")
@Data
public class SimulatorEntity {

    @Id
    private Long id;

    private String description;

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "protocol_type")
    private String protocolType;

    @Column(name = "snmp_version")
    private String snmpVersion;

    @OneToMany(mappedBy = "simulator", fetch = FetchType.LAZY)
    private List<DeviceDataEntity> dataPoints;
}