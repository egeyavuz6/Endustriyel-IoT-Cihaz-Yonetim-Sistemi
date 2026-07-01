from influxdb_client import InfluxDBClient, Point
from influxdb_client.client.write_api import SYNCHRONOUS
import random
import time
import logging

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
token = "ZNUJWg8Z5I6Kat0qgVU61WuKIi768z0f1T-g-vRz2BODq1XnkOt4pTzIOgpIlXQvNOVIQ1tBV0mKhSDPNScNmQ=="
org = "myorg"
bucket = "device_telemetry"

client = InfluxDBClient(url=url, token=token, org=org)
write_api = client.write_api(write_options=SYNCHRONOUS)

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
    """InfluxDB'ye yazma işlemini, hata durumunda tekrar deneyerek yapar."""
    attempt = 1
    while attempt <= max_retries:
        try:
            write_api.write(bucket=bucket, org=org, record=points)
            logger.info(f"{len(points)} veri noktası başarıyla yazıldı. (Deneme: {attempt})")
            return True
        except Exception as e:
            logger.error(f"Yazma hatası (Deneme {attempt}/{max_retries}): {e}")
            if attempt < max_retries:
                logger.info(f"{delay_seconds} saniye sonra tekrar denenecek...")
                time.sleep(delay_seconds)
            attempt += 1

    logger.error("Tüm deneme hakları tükendi, veri yazılamadı.")
    return False

BATCH_SIZE = 4
points_batch = []

logger.info("Simülasyon başlatıldı.")

for i in range(BATCH_SIZE):
    point = generate_sensor_data(device_id=5)
    points_batch.append(point)
    logger.info(f"Veri üretildi ({i+1}/{BATCH_SIZE}): {point.to_line_protocol()}")
    time.sleep(3)

logger.info(f"{len(points_batch)} veri noktası gönderiliyor...")
write_with_retry(points_batch)

client.close()
logger.info("Simülasyon tamamlandı, bağlantı kapatıldı.")