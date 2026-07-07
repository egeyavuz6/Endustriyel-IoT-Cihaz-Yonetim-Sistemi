package com.argela.iot_device_management.dto;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String email;
    private String password;
    private String role;   // "ADMIN", "OPERATOR", "VIEWER"
}