document.addEventListener("DOMContentLoaded", async function () {
    if (!localStorage.getItem("access_token")) {
        window.location.href = "index.html";
        return;
    }

    await loadDevices();

    document.getElementById("createDeviceForm").addEventListener("submit", async function (event) {
        event.preventDefault();
        await handleCreateDevice();
    });
});

async function loadDevices() {
    try {
        const devices = await getAllDevices();
        renderDevicesTable(devices);
    } catch (error) {
        console.error(error);
        alert("Cihazlar yüklenirken bir hata oluştu.");
    }
}

async function handleCreateDevice() {
    const formMessage = document.getElementById("formMessage");

    const deviceData = {
        name: document.getElementById("deviceName").value,
        serialNumber: document.getElementById("serialNumber").value,
        type: document.getElementById("deviceType").value,
        location: document.getElementById("location").value,
        status: "ACTIVE"
    };

    try {
        await createDevice(deviceData);
        formMessage.textContent = "Cihaz başarıyla eklendi!";
        formMessage.style.color = "green";

        document.getElementById("createDeviceForm").reset();

        await loadDevices();

    } catch (error) {
        formMessage.textContent = "Hata: " + error.message;
        formMessage.style.color = "red";
    }
}

function renderDevicesTable(devices) {
    const tableBody = document.getElementById("devicesTableBody");
    tableBody.innerHTML = "";

    devices.forEach(function (device) {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${device.id}</td>
            <td>${device.name}</td>
            <td>${device.serialNumber}</td>
            <td>${device.type}</td>
            <td>${device.location}</td>
            <td>${device.status}</td>
            <td><a href="device-detail.html?id=${device.id}">Detay</a></td>
        `;
        tableBody.appendChild(row);
    });
}