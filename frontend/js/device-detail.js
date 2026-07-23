const urlParams = new URLSearchParams(window.location.search);
const deviceId = urlParams.get("id");

document.addEventListener("DOMContentLoaded", async function () {
    if (!localStorage.getItem("access_token")) {
        window.location.href = "index.html";
        return;
    }

    await loadDeviceInfo();
    await loadCommands();
    await loadTemperatureChart();
});

async function loadDeviceInfo() {
    const device = await getDeviceById(deviceId);
    document.getElementById("deviceTitle").textContent = `Cihaz: ${device.name}`;
    document.getElementById("deviceInfo").innerHTML = `
        <p><strong>Seri No:</strong> ${device.serialNumber}</p>
        <p><strong>Tip:</strong> ${device.type}</p>
        <p><strong>Lokasyon:</strong> ${device.location}</p>
        <p><strong>Durum:</strong> ${device.status}</p>
    `;
}

async function loadCommands() {
    const commands = await getDeviceCommands(deviceId);
    const tableBody = document.getElementById("commandsTableBody");
    tableBody.innerHTML = "";

    commands.forEach(function (command) {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${command.commandType}</td>
            <td>${command.status}</td>
            <td>${command.createdAt}</td>
        `;
        tableBody.appendChild(row);
    });
}

async function handleSendCommand(commandType) {
    const messageEl = document.getElementById("commandMessage");
    try {
        await sendCommand(deviceId, commandType);
        messageEl.textContent = `${commandType} komutu gönderildi!`;
        messageEl.style.color = "green";
        await loadCommands();
    } catch (error) {
        messageEl.textContent = "Hata: " + error.message;
        messageEl.style.color = "red";
    }
}