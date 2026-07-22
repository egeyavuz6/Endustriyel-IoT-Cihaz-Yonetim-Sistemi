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