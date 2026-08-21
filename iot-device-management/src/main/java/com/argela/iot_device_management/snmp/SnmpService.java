package com.argela.iot_device_management.snmp;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.event.ResponseListener;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.function.Consumer;

@Service
public class SnmpService {

    private Snmp snmp;

    @PostConstruct
    public void init() throws IOException {
        TransportMapping<?> transport = new DefaultUdpTransportMapping();
        snmp = new Snmp(transport);
        transport.listen();
    }

    @PreDestroy
    public void shutdown() throws IOException {
        if (snmp != null) {
            snmp.close();
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

    private PDU buildGetPdu(String oid) {
        PDU pdu = new PDU();
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(PDU.GET);
        return pdu;
    }

    public String getOid(String ip, int port, String community, String oid) {
        try {
            CommunityTarget target = buildTarget(ip, port, community);
            PDU pdu = buildGetPdu(oid);

            ResponseEvent response = snmp.get(pdu, target);

            if (response.getResponse() == null) {
                return "Yanit alinamadi (timeout).";
            }

            return response.getResponse().getVariableBindings().get(0).toString();

        } catch (IOException e) {
            return "Hata: " + e.getMessage();
        }
    }

    public void getOidAsync(String ip, int port, String community, String oid, Consumer<String> onResult) {
        CommunityTarget target = buildTarget(ip, port, community);
        PDU pdu = buildGetPdu(oid);

        ResponseListener listener = new ResponseListener() {
            @Override
            public void onResponse(ResponseEvent event) {
                ((Snmp) event.getSource()).cancel(event.getRequest(), this);

                if (event.getResponse() == null) {
                    onResult.accept("Yanit alinamadi (timeout).");
                } else {
                    onResult.accept(event.getResponse().getVariableBindings().get(0).toString());
                }
            }
        };

        try {
            snmp.send(pdu, target, null, listener);
        } catch (IOException e) {
            onResult.accept("Hata: " + e.getMessage());
        }
    }
}