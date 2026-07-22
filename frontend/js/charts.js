async function loadTemperatureChart() {
    try {
        const telemetryData = await getDeviceTelemetry(deviceId, 24);

        const temperaturePoints = telemetryData.filter(function (point) {
            return point.field === "temperature";
        });

        const labels = temperaturePoints.map(function (point) {
            return new Date(point.time).toLocaleTimeString();
        });

        const values = temperaturePoints.map(function (point) {
            return point.value;
        });

        const ctx = document.getElementById("temperatureChart").getContext("2d");

        new Chart(ctx, {
            type: "line",
            data: {
                labels: labels,
                datasets: [{
                    label: "Sıcaklık (°C)",
                    data: values,
                    borderColor: "#2563eb",
                    backgroundColor: "rgba(37, 99, 235, 0.1)",
                    fill: true,
                    tension: 0.3
                }]
            },
            options: {
                responsive: true,
                scales: {
                    y: { beginAtZero: false }
                }
            }
        });

    } catch (error) {
        console.error("Grafik yüklenemedi:", error);
    }
}