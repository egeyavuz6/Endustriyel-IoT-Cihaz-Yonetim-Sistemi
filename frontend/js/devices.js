document.addEventListener("DOMContentLoaded", async function () {
    if (!localStorage.getItem("access_token")) {
        window.location.href = "index.html";
        return;
    }

    document.getElementById("logoutButton").addEventListener("click", function () {
        localStorage.removeItem("access_token");
        window.location.href = "index.html";
    });

    await loadDevices();

    document.getElementById("createDeviceForm").addEventListener("submit", async function (event) {
        event.preventDefault();
        await handleCreateDevice();
    });
});

async function loadDevices() {
    try {
        const devices = await getDevicesWithStatus();
        renderDevicesTable(devices);
    } catch (error) {
        console.error(error);
        alert("Cihazlar yuklenirken hata olustu.");
    }
}

async function handleCreateDevice() {
    const formMessage = document.getElementById("formMessage");

    const deviceData = {
        name: document.getElementById("deviceName").value,
        serialNumber: document.getElementById("serialNumber").value,
        type: document.getElementById("deviceType").value,
        location: document.getElementById("location").value
    };

    try {
        await createDevice(deviceData);
        formMessage.textContent = "Cihaz basariyla eklendi!";
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
        const statusClass = device.status === "ACTIVE" ? "status-active" : "status-passive";
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${device.id}</td>
            <td>${device.name}</td>
            <td>${device.serialNumber}</td>
            <td>${device.type}</td>
            <td>${device.location}</td>
            <td class="${statusClass}">${device.status}</td>
            <td><a href="device-detail.html?id=${device.id}">Detay</a></td>
        `;
        tableBody.appendChild(row);
    });
}

let currentDevices = [];
let sortDirection = {};

async function loadDevices() {
    try {
        currentDevices = await getDevicesWithStatus();
        renderDevicesTable(currentDevices);
    } catch (error) {
        console.error(error);
        alert("Cihazlar yuklenirken hata olustu.");
    }
}

function sortTable(field) {
    const direction = sortDirection[field] === "asc" ? "desc" : "asc";
    sortDirection = { [field]: direction };

    currentDevices.sort(function (a, b) {
        const valA = a[field];
        const valB = b[field];

        if (valA < valB) return direction === "asc" ? -1 : 1;
        if (valA > valB) return direction === "asc" ? 1 : -1;
        return 0;
    });

    renderDevicesTable(currentDevices);
}