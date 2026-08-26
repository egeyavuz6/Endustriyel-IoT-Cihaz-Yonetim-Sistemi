package com.argela.iot_device_management.snmp;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DockerDiscoveryService {

    private static final Logger log = LoggerFactory.getLogger(DockerDiscoveryService.class);

    private final DockerClient dockerClient;
    private final Map<String, Integer> portCache = new ConcurrentHashMap<>();

    public DockerDiscoveryService(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    public Optional<Integer> findHostPortByInternalIp(String internalIp, int containerPort) {
        // 1. Önce Cache'e bak
        if (portCache.containsKey(internalIp)) {
            return Optional.of(portCache.get(internalIp));
        }

        // 2. Cache'te yoksa Docker'a sor (Miss)
        return refreshAndFindPort(internalIp, containerPort);
    }

    public synchronized Optional<Integer> refreshAndFindPort(String internalIp, int containerPort) {
        try {
            List<Container> containers = dockerClient.listContainersCmd().exec();

            for (Container container : containers) {
                Map<String, ContainerNetwork> networks = container.getNetworkSettings().getNetworks();

                boolean matches = networks.values().stream()
                        .anyMatch(net -> internalIp.equals(net.getIpAddress()));

                if (matches) {
                    Optional<Integer> hostPort = findHostPort(container, containerPort);
                    hostPort.ifPresent(port -> portCache.put(internalIp, port));
                    return hostPort;
                }
            }
        } catch (Exception e) {
            log.error("Docker container listesi alınırken hata oluştu: {}", e.getMessage());
        }

        return Optional.empty();
    }

    private Optional<Integer> findHostPort(Container container, int containerPort) {
        if (container.getPorts() == null) return Optional.empty();

        return List.of(container.getPorts()).stream()
                .filter(p -> p.getPrivatePort() != null && p.getPrivatePort() == containerPort)
                .filter(p -> p.getPublicPort() != null)
                .map(p -> p.getPublicPort())
                .findFirst();
    }

    public void clearCache() { // Önbelleği temizlemek için.
        portCache.clear();
    }
}