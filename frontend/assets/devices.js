document.addEventListener("DOMContentLoaded", async function () {
    if (!localStorage.getItem("access_token")) {
        window.location.href = "index.html";
        return;
    }

    try {
        const devices = await getAllDevices();
        renderDevicesTable(devices);
    } catch (error) {
        console.error(error);
        alert("Cihazlar yüklenirken bir hata oluştu.");
    }
});

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