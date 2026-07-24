const API_BASE_URL = "http://localhost:8080";

function getAuthHeaders() {
    const token = localStorage.getItem("access_token");
    return {
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json"
    };
}

async function getAllDevices() {
    const response = await fetch(`${API_BASE_URL}/api/devices`, {
        method: "GET",
        headers: getAuthHeaders()
    });

    if (!response.ok) {
        throw new Error("Cihazlar yüklenemedi.");
    }

    return await response.json();
}

async function createDevice(deviceData) {
    const response = await fetch(`${API_BASE_URL}/api/devices`, {
        method: "POST",
        headers: getAuthHeaders(),
        body: JSON.stringify(deviceData)
    });

    if (!response.ok) {
        throw new Error("Cihaz oluşturulamadı.");
    }

    return await response.json();
}

async function getDeviceById(id) {
    const response = await fetch(`${API_BASE_URL}/api/devices/${id}`, {
        method: "GET",
        headers: getAuthHeaders()
    });
    if (!response.ok) throw new Error("Cihaz bilgisi alınamadı.");
    return await response.json();
}

async function getDeviceCommands(id) {
    const response = await fetch(`${API_BASE_URL}/api/devices/${id}/commands`, {
        method: "GET",
        headers: getAuthHeaders()
    });
    if (!response.ok) throw new Error("Komutlar alınamadı.");
    return await response.json();
}

async function sendCommand(deviceId, commandType) {
    const response = await fetch(`${API_BASE_URL}/api/commands`, {
        method: "POST",
        headers: getAuthHeaders(),
        body: JSON.stringify({
            device: { id: deviceId },
            commandType: commandType
        })
    });
    if (!response.ok) throw new Error("Komut gönderilemedi.");
    return await response.json();
}

async function getDeviceTelemetry(id, hours = 24) {
    const response = await fetch(`${API_BASE_URL}/api/devices/${id}/telemetry?hours=${hours}`, {
        method: "GET",
        headers: getAuthHeaders()
    });
    if (!response.ok) throw new Error("Telemetry verisi alınamadı.");
    return await response.json();
}

async function getActiveAlarms() {
    const response = await fetch(`${API_BASE_URL}/api/commands/alarms`, {
        method: "GET",
        headers: getAuthHeaders()
    });
    if (!response.ok) throw new Error("Alarmlar yuklenemedi.");
    return await response.json();
}

async function getRecentActivity() {
    const response = await fetch(`${API_BASE_URL}/api/command-logs/recent`, {
        method: "GET",
        headers: getAuthHeaders()
    });
    if (!response.ok) throw new Error("Son aktiviteler yuklenemedi.");
    return await response.json();
}

async function getDevicesByLocation() {
    const response = await fetch(`${API_BASE_URL}/api/devices/by-location`, {
        method: "GET",
        headers: getAuthHeaders()
    });
    if (!response.ok) throw new Error("Lokasyon verisi yuklenemedi.");
    return await response.json();
}