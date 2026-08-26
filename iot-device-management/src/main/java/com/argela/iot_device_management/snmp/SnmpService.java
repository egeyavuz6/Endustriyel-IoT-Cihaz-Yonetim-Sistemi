package com.argela.iot_device_management.snmp;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@Service
public class SnmpService {

    private static final Logger log = LoggerFactory.getLogger(SnmpService.class);
    private Snmp snmp;

    @PostConstruct
    public void init() throws IOException {
        TransportMapping<?> transport = new DefaultUdpTransportMapping();
        snmp = new Snmp(transport);
        transport.listen();
        log.info("SNMP Servisi UDP portu üzerinde başlatıldı.");
    }

    @PreDestroy
    public void shutdown() {
        if (snmp != null) {
            try {
                snmp.close();
                log.info("SNMP Servisi kapatıldı.");
            } catch (IOException e) {
                log.error("SNMP servisi kapatılırken hata oluştu: {}", e.getMessage());
            }
        }
    }

    private CommunityTarget buildTarget(String ip, int port, String community) {
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(GenericAddress.parse("udp:" + ip + "/" + port));
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);
        return target;
    }

    private PDU buildGetPdu(List<String> oids) {
        PDU pdu = new PDU();
        for (String oid : oids) {
            pdu.add(new VariableBinding(new OID(oid)));
        }
        pdu.setType(PDU.GET);
        return pdu;
    }

    public Optional<String> getOid(String ip, int port, String community, String oid) {
        try {
            CommunityTarget target = buildTarget(ip, port, community);
            PDU pdu = buildGetPdu(List.of(oid));

            ResponseEvent response = snmp.get(pdu, target);

            if (response.getResponse() == null || response.getResponse().getVariableBindings().isEmpty()) {
                log.warn("SNMP yanıt alınamadı (Timeout). IP: {}:{}", ip, port);
                return Optional.empty();
            }

            return Optional.ofNullable(response.getResponse().getVariableBindings().get(0).getVariable().toString());

        } catch (IOException e) {
            log.error("SNMP GET isteği sırasında hata: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public void getMultipleOidsAsync(String ip, int port, String community, List<String> oids, Consumer<Map<String, String>> onResult) {
        CommunityTarget target = buildTarget(ip, port, community);
        PDU pdu = buildGetPdu(oids);

        ResponseListener listener = new ResponseListener() {
            @Override
            public void onResponse(ResponseEvent event) {
                ((Snmp) event.getSource()).cancel(event.getRequest(), this);

                Map<String, String> results = new HashMap<>();

                if (event.getResponse() == null) {
                    log.warn("Asenkron SNMP yanıt alınamadı (Timeout). IP: {}:{}", ip, port);
                    onResult.accept(results);
                    return;
                }

                for (VariableBinding vb : event.getResponse().getVariableBindings()) {
                    if (vb != null && vb.getVariable() != null) {
                        results.put(vb.getOid().toString(), vb.getVariable().toString());
                    }
                }

                onResult.accept(results);
            }
        };

        try {
            snmp.send(pdu, target, null, listener);
        } catch (IOException e) {
            log.error("Asenkron SNMP isteği gönderilemedi: {}", e.getMessage());
            onResult.accept(new HashMap<>());
        }
    }
}