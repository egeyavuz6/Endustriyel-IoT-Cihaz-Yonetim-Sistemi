from influxdb_client import InfluxDBClient, Point
from influxdb_client.client.write_api import SYNCHRONOUS
import random
import time
import logging
import requests

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    handlers=[
        logging.FileHandler("simulator.log"),
        logging.StreamHandler()
    ]
)
logger = logging.getLogger(__name__)

url = "http://localhost:8086"
token = "MvE2nW6Z4GSjTJGsYY5r0gWmGbTXPSqTb3b9cc_e2BkzVmxFDA9mCsmoWcDzO-3s7WYFkLeaEXKk3O219y4atw=="
org = "myorg"
bucket = "device_telemetry"
SPRING_BOOT_API = "http://localhost:8080/api/devices"

client = InfluxDBClient(url=url, token=token, org=org)
write_api = client.write_api(write_options=SYNCHRONOUS)

KEYCLOAK_TOKEN_URL = "http://localhost:8180/realms/iot-device-management/protocol/openid-connect/token"
CLIENT_ID = "iot-backend"
CLIENT_SECRET = "pBZ9oQrl0cePqS03OE7oi77op43hYhxm"
USERNAME = "ege_admin"
PASSWORD = "egeruzgar02"

def get_access_token():
    data = {
        "client_id": CLIENT_ID,
        "client_secret": CLIENT_SECRET,
        "username": USERNAME,
        "password": PASSWORD,
        "grant_type": "password"
    }
    response = requests.post(KEYCLOAK_TOKEN_URL, data=data)
    response.raise_for_status()
    token = response.json()["access_token"]
    logger.info("Keycloak'tan token alındı.")
    return token

def get_all_device_ids(token):
    try:
        headers = {"Authorization": f"Bearer {token}"}
        response = requests.get(SPRING_BOOT_API, headers=headers)
        response.raise_for_status()
        devices = response.json()
        device_ids = [device["id"] for device in devices]
        logger.info(f"Spring Boot'tan {len(device_ids)} cihaz bulundu: {device_ids}")
        return device_ids
    except Exception as e:
        logger.error(f"Cihaz listesi alınamadı: {e}")
        return []

def generate_sensor_data(device_id):
    point = (
        Point("device_telemetry")
        .tag("device_id", str(device_id))
        .field("temperature", round(random.uniform(15.0, 35.0), 2))
        .field("humidity", round(random.uniform(30.0, 70.0), 2))
        .field("pressure", round(random.uniform(1000.0, 1020.0), 2))
        .field("vibration", round(random.uniform(0.0, 5.0), 2))
    )
    return point

def write_with_retry(points, max_retries=3, delay_seconds=2):
    attempt = 1
    while attempt <= max_retries:
        try:
            write_api.write(bucket=bucket, org=org, record=points)
            logger.info(f"{len(points)} veri başarıyla yazıldı. (Deneme: {attempt})")
            return True
        except Exception as e:
            logger.error(f"Yazma hatası (Deneme {attempt}/{max_retries}): {e}")
            if attempt < max_retries:
                logger.info(f"{delay_seconds} saniye sonra tekrar denenecek...")
                time.sleep(delay_seconds)
            attempt += 1

    logger.error("Tüm deneme hakları tükendi, veri yazılamadı.")
    return False

BATCH_WRITE_SIZE = 10   # her 10 veri noktasında bir InfluxDB'ye yaz
DURATION_SECONDS = 6 * 60 * 60   # 6 saat
INTERVAL_SECONDS = 3   # her 3 saniyede bir veri üret

logger.info("Simülasyon başlatıldı. 6 saat boyunca çalışacak.")

keycloak_token = get_access_token()
device_ids = get_all_device_ids(keycloak_token)

if not device_ids:
    logger.error("Hiç cihaz bulunamadı, simülasyon durduruluyor.")
else:
    start_time = time.time()
    points_batch = []

    while time.time() - start_time < DURATION_SECONDS:
        for device_id in device_ids:
            point = generate_sensor_data(device_id=device_id)
            points_batch.append(point)
            logger.info(f"Veri üretildi (device_id={device_id}): {point.to_line_protocol()}")

        # Belirli bir sayıya ulaşınca InfluxDB'ye yaz
        if len(points_batch) >= BATCH_WRITE_SIZE:
            write_with_retry(points_batch)
            points_batch = []   # listeyi boşalt

        time.sleep(INTERVAL_SECONDS)

    # Döngü bitince, kalan (henüz yazılmamış) veri varsa onu da yaz
    if points_batch:
        write_with_retry(points_batch)

client.close()
logger.info("Simülasyon tamamlandı, bağlantı kapatıldı.")