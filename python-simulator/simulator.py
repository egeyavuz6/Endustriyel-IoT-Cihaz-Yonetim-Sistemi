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
token = "wS88u1JEbCx3spiP26u3BEfoVMsBkXRV5xyBX3N9dbGvjdagTNFvUy1Zzo0-IkwSkHzwbb_iIri-1ppjEq1_nw=="
org = "myorg"
bucket = "device_telemetry"
SPRING_BOOT_API = "http://localhost:8080/api/devices"

client = InfluxDBClient(url=url, token=token, org=org)
write_api = client.write_api(write_options=SYNCHRONOUS)

def get_all_device_ids():
    try:
        response = requests.get(SPRING_BOOT_API)
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

BATCH_SIZE = 4
logger.info("Simülasyon başlatıldı.")

device_ids = get_all_device_ids()

if not device_ids:
    logger.error("Hiç cihaz bulunamadı, simülasyon durduruluyor.")
else:
    points_batch = []

    for i in range(BATCH_SIZE):
        for device_id in device_ids:
            point = generate_sensor_data(device_id=device_id)
            points_batch.append(point)
            logger.info(f"Veri üretildi (device_id={device_id}): {point.to_line_protocol()}")
        time.sleep(3)

    logger.info(f"{len(points_batch)} veri noktası gönderiliyor...")
    write_with_retry(points_batch)

client.close()
logger.info("Simülasyon tamamlandı, bağlantı kapatıldı.")