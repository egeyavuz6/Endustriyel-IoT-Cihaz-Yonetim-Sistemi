package com.argela.iot_device_management.snmp;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.ContainerNetwork;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class DockerDiscoveryService {

    private final DockerClient dockerClient;

    public DockerDiscoveryService() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost("tcp://localhost:2375")
                .build();

        ApacheDockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .build();

        this.dockerClient = DockerClientImpl.getInstance(config, httpClient);
    }


    public Optional<Integer> findHostPortByInternalIp(String internalIp, int containerPort) {
        List<Container> containers = dockerClient.listContainersCmd().exec();

        for (Container container : containers) {
            Map<String, ContainerNetwork> networks = container.getNetworkSettings().getNetworks();

            boolean matches = networks.values().stream()
                    .anyMatch(net -> internalIp.equals(net.getIpAddress()));

            if (matches) {
                return findHostPort(container, containerPort);
            }
        }

        return Optional.empty();
    }

    private Optional<Integer> findHostPort(Container container, int containerPort) {
        return List.of(container.getPorts()).stream()
                .filter(p -> p.getPrivatePort() != null && p.getPrivatePort() == containerPort)
                .filter(p -> p.getPublicPort() != null)
                .map(p -> p.getPublicPort())
                .findFirst();
    }
}