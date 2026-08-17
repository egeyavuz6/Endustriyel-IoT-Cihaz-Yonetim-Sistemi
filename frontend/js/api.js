const API_BASE_URL = "http://localhost:8080";

function getAuthHeaders() {
    const token = localStorage.getItem("access_token");
    return {
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json"
    };
}

async function getDevicesWithStatus() {
    const response = await fetch(`${API_BASE_URL}/api/devices/with-status`, {
        method: "GET",
        headers: getAuthHeaders()
    });
    if (!response.ok) throw new Error("Cihazlar yuklenemedi.");
    return await response.json();
}

async function getDeviceById(id) {
    const response = await fetch(`${API_BASE_URL}/api/devices/${id}`, {
        method: "GET",
        headers: getAuthHeaders()
    });
    if (!response.ok) throw new Error("Cihaz bilgisi alinamadi.");
    return await response.json();
}

async function createDevice(deviceData) {
    const response = await fetch(`${API_BASE_URL}/api/devices`, {
        method: "POST",
        headers: getAuthHeaders(),
        body: JSON.stringify(deviceData)
    });
    if (!response.ok) throw new Error("Cihaz olusturulamadi.");
    return await response.json();
}

async function getDeviceCommands(deviceId) {
    const response = await fetch(`${API_BASE_URL}/api/commands/device/${deviceId}`, {
        method: "GET",
        headers: getAuthHeaders()
    });
    if (!response.ok) throw new Error("Komutlar alinamadi.");
    return await response.json();
}

async function getDeviceCommandLogs(deviceId) {
    const response = await fetch(`${API_BASE_URL}/api/devices/${deviceId}/commands`, {
        method: "GET",
        headers: getAuthHeaders()
    });
    if (!response.ok) throw new Error("Komut gecmisi alinamadi.");
    return await response.json();
}

async function sendCommand(deviceId, commandId, commandValue = null) {
    const response = await fetch(`${API_BASE_URL}/api/command-logs/send-command`, {
        method: "POST",
        headers: getAuthHeaders(),
        body: JSON.stringify({ deviceId, commandId, commandValue })
    });
    if (!response.ok) {
        const err = await response.json();
        throw new Error(err.message || "Komut gonderilemedi.");
    }
    return await response.json();
}

async function updateTelemetrySettings(commandId, settings) {
    const response = await fetch(`${API_BASE_URL}/api/commands/${commandId}/telemetry-settings`, {
        method: "PUT",
        headers: getAuthHeaders(),
        body: JSON.stringify(settings)
    });
    if (!response.ok) throw new Error("Ayarlar guncellenemedi.");
    return await response.json();
}