package com.argela.iot_device_management.controller;

import com.argela.iot_device_management.dto.CreateUserRequest;
import com.argela.iot_device_management.service.KeycloakAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {

    private final KeycloakAdminService keycloakAdminService;

    public AdminController(KeycloakAdminService keycloakAdminService) {
        this.keycloakAdminService = keycloakAdminService;
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/admin/users")
    public ResponseEntity<String> createUser(@RequestBody CreateUserRequest request) {
        keycloakAdminService.createUser(
                request.getUsername(),
                request.getEmail(),
                request.getPassword(),
                request.getRole()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body("Kullanıcı oluşturuldu.");
    }
}