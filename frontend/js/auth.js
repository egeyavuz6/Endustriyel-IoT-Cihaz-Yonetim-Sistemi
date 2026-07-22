const KEYCLOAK_URL = "http://localhost:8180";
const REALM = "iot-device-management";
const CLIENT_ID = "iot-backend";
const CLIENT_SECRET = "1h1CUC3kXAzTD1n8x295cRW3eMyiyADv";

document.getElementById("loginForm").addEventListener("submit", async function (event) {
    event.preventDefault();

    const username = document.getElementById("username").value;
    const password = document.getElementById("password").value;
    const errorMessage = document.getElementById("errorMessage");

    const tokenUrl = `${KEYCLOAK_URL}/realms/${REALM}/protocol/openid-connect/token`;

    const params = new URLSearchParams();
    params.append("client_id", CLIENT_ID);
    params.append("client_secret", CLIENT_SECRET);
    params.append("username", username);
    params.append("password", password);
    params.append("grant_type", "password");

    try {
        const response = await fetch(tokenUrl, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: params
        });

        if (!response.ok) {
            throw new Error("Giriş başarısız. Kullanıcı adı veya şifre hatalı.");
        }

        const data = await response.json();
        localStorage.setItem("access_token", data.access_token);

        window.location.href = "dashboard.html";

    } catch (error) {
        errorMessage.textContent = error.message;
        errorMessage.style.display = "block";
    }
});