document.addEventListener("DOMContentLoaded", async function () {
    if (!localStorage.getItem("access_token")) {
        window.location.href = "index.html";
        return;
    }

    document.getElementById("logoutButton").addEventListener("click", function () {
        localStorage.removeItem("access_token");
        window.location.href = "index.html";
    });

    await loadLocationOverview();
});

async function loadLocationOverview() {
    try {
        const locationData = await getDevicesByLocation();
        const container = document.getElementById("locationOverview");
        container.innerHTML = "";

        for (const [location, summary] of Object.entries(locationData)) {
            const card = document.createElement("div");
            card.className = "location-card";
            card.innerHTML = `
                <strong>${location}</strong>
                <p>Toplam: ${summary.totalDevices} | Aktif: ${summary.activeDevices} | Pasif: ${summary.passiveDevices}</p>
            `;
            container.appendChild(card);
        }
    } catch (error) {
        console.error("Lokasyon verisi yuklenemedi:", error);
    }
}