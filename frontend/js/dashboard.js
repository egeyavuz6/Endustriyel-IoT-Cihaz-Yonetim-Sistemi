document.addEventListener("DOMContentLoaded", async function () {
    if (!localStorage.getItem("access_token")) {
        window.location.href = "index.html";
        return;
    }

    document.getElementById("logoutButton").addEventListener("click", function () {
        localStorage.removeItem("access_token");
        window.location.href = "index.html";
    });

    await loadSystemHealth();
    await loadActiveAlarms();
    await loadRecentActivity();
});

async function loadSystemHealth() {
    try {
        const devices = await getAllDevices();
        const alarms = await getActiveAlarms();

        const total = devices.length;
        const active = devices.filter(d => d.status === "ACTIVE").length;
        const passive = devices.filter(d => d.status === "PASSIVE").length;

        document.getElementById("totalDevices").textContent = total;
        document.getElementById("activeDevices").textContent = active;
        document.getElementById("passiveDevices").textContent = passive;
        document.getElementById("activeAlarmsCount").textContent = alarms.length;

        renderDeviceStatusList(devices);
    } catch (error) {
        console.error("Sistem durumu yuklenemedi:", error);
    }
}

async function loadActiveAlarms() {
    try {
        const alarms = await getActiveAlarms();
        const container = document.getElementById("alarmsList");
        container.innerHTML = "";

        if (alarms.length === 0) {
            container.innerHTML = "<p class='no-alarms'>Su an aktif alarm yok.</p>";
            return;
        }

        alarms.forEach(function (alarm) {
            const card = document.createElement("div");
            card.className = "alarm-card";
            card.innerHTML = `
                <strong>${alarm.commandType}</strong>
                <p>Esik degeri: ${alarm.thresholdValue}</p>
            `;
            container.appendChild(card);
        });
    } catch (error) {
        console.error("Alarmlar yuklenemedi:", error);
    }
}

function renderDeviceStatusList(devices) {
    const container = document.getElementById("deviceStatusList");
    container.innerHTML = "";

    devices.forEach(function (device) {
        const statusClass = device.status === "ACTIVE" ? "status-active" : "status-passive";
        const statusIcon = device.status === "ACTIVE" ? "🟢" : "⚪";

        const item = document.createElement("a");
        item.href = `device-detail.html?id=${device.id}`;
        item.className = `device-status-item ${statusClass}`;
        item.innerHTML = `
            ${statusIcon} <strong>${device.name}</strong> - ${device.type} - ${device.location}
        `;
        container.appendChild(item);
    });
}

async function loadRecentActivity() {
    try {
        const logs = await getRecentActivity();
        const container = document.getElementById("recentActivityList");
        container.innerHTML = "";

        if (logs.length === 0) {
            container.innerHTML = "<p>Henuz aktivite yok.</p>";
            return;
        }

        logs.forEach(function (log) {
            const item = document.createElement("div");
            item.className = "activity-item";
            const time = new Date(log.createdAt).toLocaleString();
            item.innerHTML = `
                <span class="activity-time">${time}</span>
                <span>${log.command.commandType} - Cihaz ${log.device.name} - ${log.status}</span>
            `;
            container.appendChild(item);
        });
    } catch (error) {
        console.error("Son aktiviteler yuklenemedi:", error);
    }
}