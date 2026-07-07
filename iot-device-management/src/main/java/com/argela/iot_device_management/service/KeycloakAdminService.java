package com.argela.iot_device_management.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class KeycloakAdminService {

    @Value("${keycloak.admin.server-url}")
    private String serverUrl;

    @Value("${keycloak.admin.realm}")
    private String adminRealm;

    @Value("${keycloak.admin.client-id}")
    private String adminClientId;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    @Value("${keycloak.target-realm}")
    private String targetRealm;

    private final RestTemplate restTemplate = new RestTemplate();

    private String getAdminAccessToken() {
        String tokenUrl = serverUrl + "/realms/" + adminRealm + "/protocol/openid-connect/token";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", adminClientId);
        body.add("username", adminUsername);
        body.add("password", adminPassword);
        body.add("grant_type", "password");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        Map response = restTemplate.postForObject(tokenUrl, request, Map.class);
        return (String) response.get("access_token");
    }

    public void createUser(String username, String email, String password, String role) {
        String adminToken = getAdminAccessToken();

        // 1. ÖNCE rolü doğrula (kullanıcı oluşturmadan önce)
        Map<String, Object> roleDetails = getRoleDetails(role, adminToken);

        // 2. Rol geçerliyse, kullanıcıyı oluştur
        String createUserUrl = serverUrl + "/admin/realms/" + targetRealm + "/users";

        Map<String, Object> credentials = Map.of(
                "type", "password",
                "value", password,
                "temporary", false
        );

        Map<String, Object> userPayload = Map.of(
                "username", username,
                "email", email,
                "enabled", true,
                "credentials", java.util.List.of(credentials)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(userPayload, headers);
        restTemplate.postForEntity(createUserUrl, request, Void.class);

        // 3. Kullanıcının ID'sini bul
        String userId = getUserIdByUsername(username, adminToken);

        // 4. Rolü ata
        assignRoleToUser(userId, roleDetails, adminToken);
    }

    private String getUserIdByUsername(String username, String adminToken) {
        String url = serverUrl + "/admin/realms/" + targetRealm + "/users?username=" + username;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, request, List.class);
        Map<String, Object> user = (Map<String, Object>) response.getBody().get(0);
        return (String) user.get("id");
    }

    private Map<String, Object> getRoleDetails(String roleName, String adminToken) {
        String url = serverUrl + "/admin/realms/" + targetRealm + "/roles/" + roleName;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
        return response.getBody();
    }

    private void assignRoleToUser(String userId, Map<String, Object> roleDetails, String adminToken) {
        String url = serverUrl + "/admin/realms/" + targetRealm + "/users/" + userId + "/role-mappings/realm";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(List.of(roleDetails), headers);
        restTemplate.postForEntity(url, request, Void.class);
    }
}