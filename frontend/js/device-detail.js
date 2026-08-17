const urlParams = new URLSearchParams(window.location.search);
const deviceId = urlParams.get("id");

document.addEventListener("DOMContentLoaded", async function () {
    if (!localStorage.getItem("access_token")) {
        window.location.href = "index.html";
        return;
    }

    await loadDeviceInfo();
    await loadCommands();
    await loadLogs();
});

async function loadDeviceInfo() {
    const device = await getDeviceById(deviceId);
    document.getElementById("deviceTitle").textContent = `Cihaz: ${device.name}`;
    document.getElementById("deviceInfo").innerHTML = `
        <p><strong>Seri No:</strong> ${device.serialNumber}</p>
        <p><strong>Tip:</strong> ${device.type}</p>
        <p><strong>Lokasyon:</strong> ${device.location}</p>
    `;
}

async function loadCommands() {
    const commands = await getDeviceCommands(deviceId);
    const container = document.getElementById("commandsList");
    container.innerHTML = "";

    commands.forEach(function (cmd) {
        const card = document.createElement("div");
        card.className = "command-card";

        let actionHtml = "";
        if (cmd.operationType === "WRITE" || cmd.operationType === "R/W") {
            actionHtml = `<button onclick="handleSendCommand(${cmd.id})">Gonder</button>`;
        }

        let stateHtml = "";
        if (cmd.currentState) {
            stateHtml = `<span class="current-state">Durum: ${cmd.currentState}</span>`;
        }

        card.innerHTML = `
            <strong>${cmd.commandType}</strong> (${cmd.operationType})
            ${stateHtml}
            ${actionHtml}
        `;
        container.appendChild(card);
    });
}

async function handleSendCommand(commandId) {
    try {
        await sendCommand(parseInt(deviceId), commandId);
        alert("Komut gonderildi!");
        await loadLogs();
        setTimeout(loadCommands, 1000);
    } catch (error) {
        alert("Hata: " + error.message);
    }
}

async function loadLogs() {
    const logs = await getDeviceCommandLogs(deviceId);
    const tableBody = document.getElementById("logsTableBody");
    tableBody.innerHTML = "";

    logs.forEach(function (log) {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${log.command.commandType}</td>
            <td>${log.status}</td>
            <td>${log.createdAt}</td>
        `;
        tableBody.appendChild(row);
    });
}