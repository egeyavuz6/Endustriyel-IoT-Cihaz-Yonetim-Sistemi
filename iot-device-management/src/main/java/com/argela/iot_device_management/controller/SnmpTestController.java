package com.argela.iot_device_management.controller;

import com.argela.iot_device_management.snmp.SnmpService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/snmp")
public class SnmpTestController {

    private final SnmpService snmpService;

    public SnmpTestController(SnmpService snmpService) {
        this.snmpService = snmpService;
    }

    @GetMapping("/test-sync")
    public String testSync() {
        return snmpService.getOid("127.0.0.1", 1162, "public", "1.3.6.1.4.1.7309.5.2.9.2.1.1.17.170001.4");
    }

    @GetMapping("/test-async")
    public String testAsync() {
        snmpService.getOidAsync("127.0.0.1", 16101, "public", "1.3.6.1.2.1.1.1.0", result -> {
            System.out.println("Async sonuc: " + result);
        });
        return "Istek gonderildi, sonuc konsola yazilacak.";
    }
    @GetMapping("/test-real-data")
    public Map<String, String> testRealData() {
        Map<String, String> results = new HashMap<>();
        results.put("AC_GRID_R_VLTG", snmpService.getOid("127.0.0.1", 1162, "public", "1.3.6.1.4.1.7309.5.2.3.2.1.2.4.4.1"));
        results.put("BTRY_1_VLTG", snmpService.getOid("127.0.0.1", 1162, "public", "1.3.6.1.4.1.7309.5.2.3.2.1.2.50.1.1"));
        results.put("RECT1_SN", snmpService.getOid("127.0.0.1", 1162, "public", "1.3.6.1.4.1.7309.5.2.1.2.1.6.4.1"));
        return results;
    }
}

