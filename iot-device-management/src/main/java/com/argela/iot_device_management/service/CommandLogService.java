package com.argela.iot_device_management.service;

import com.argela.iot_device_management.entity.CommandLog;
import com.argela.iot_device_management.entity.Device;
import com.argela.iot_device_management.entity.DeviceCommand;
import com.argela.iot_device_management.entity.User;
import com.argela.iot_device_management.exception.ResourceNotFoundException;
import com.argela.iot_device_management.repository.CommandLogRepository;
import com.argela.iot_device_management.repository.DeviceCommandRepository;
import com.argela.iot_device_management.repository.UserRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CommandLogService {

    private final CommandLogRepository commandLogRepository;
    private final DeviceCommandRepository deviceCommandRepository;
    private final DeviceService deviceService;
    private final UserRepository userRepository;

    public CommandLogService(CommandLogRepository commandLogRepository,DeviceCommandRepository deviceCommandRepository,
                             DeviceService deviceService, UserRepository userRepository) {
        this.commandLogRepository = commandLogRepository;
        this.deviceCommandRepository = deviceCommandRepository;
        this.deviceService = deviceService;
        this.userRepository = userRepository;
    }

    public CommandLog createCommandLog(Long deviceId, Long commandId, String commandValue, Jwt jwt) {
        Device device = deviceService.getDeviceById(deviceId);

        DeviceCommand command = deviceCommandRepository.findById(commandId)
                .orElseThrow(() -> new ResourceNotFoundException("Command not found with id: " + commandId));

        User user = findOrCreateUser(jwt);

        CommandLog log = new CommandLog();
        log.setDevice(device);
        log.setCommand(command);
        log.setUser(user);
        log.setCommandValue(commandValue);
        log.setStatus("PENDING");
        log.setCreatedAt(LocalDateTime.now());

        return commandLogRepository.save(log);
    }

    private User findOrCreateUser(Jwt jwt) {
        String keycloakId = jwt.getSubject();

        return userRepository.findByKeycloakId(keycloakId)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setKeycloakId(keycloakId);
                    newUser.setUsername(jwt.getClaimAsString("preferred_username"));
                    newUser.setEmail(jwt.getClaimAsString("email"));
                    newUser.setRole(extractPrimaryRole(jwt));
                    newUser.setCreatedAt(LocalDateTime.now());
                    return userRepository.save(newUser);
                });
    }
    private String extractPrimaryRole(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");

        if (realmAccess == null || realmAccess.isEmpty()) {
            return "ROLE_VIEWER";
        }

        List<String> roles = (List<String>) realmAccess.get("roles");

        if (roles.contains("ROLE_ADMIN")) {
            return "ROLE_ADMIN";
        } else if (roles.contains("ROLE_OPERATOR")) {
            return "ROLE_OPERATOR";
        } else {
            return "ROLE_VIEWER";
        }
    }


    public List<CommandLog> getCommandLogsByDeviceId(Long deviceId) {
        return commandLogRepository.findByDeviceId(deviceId);
    }

    public CommandLog markAsExecuted(Long logId) {
        CommandLog log = commandLogRepository.findById(logId)
                .orElseThrow(() -> new ResourceNotFoundException("Command log not found with id: " + logId));

        log.setStatus("EXECUTED");
        log.setExecutedAt(LocalDateTime.now());

        return commandLogRepository.save(log);
    }
}