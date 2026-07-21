import time
import threading
from utils.config_loader import load_config
from utils.logger import setup_logger
from database.postgres_client import PostgresClient
from device.telemetry_generator import TelemetryGenerator
from device.command_handler import CommandHandler
from influxdb_client import InfluxDBClient
from influxdb_client.client.write_api import SYNCHRONOUS

logger = setup_logger(__name__)


def simulate_device(device_id, stop_event, telemetry_generator, command_handler, write_api, bucket, org, interval):
    while not stop_event.is_set():
        point = telemetry_generator.generate(device_id)
        write_api.write(bucket=bucket, org=org, record=point)
        command_handler.check_and_execute_commands(device_id)
        time.sleep(interval)
    logger.info(f"Cihaz {device_id} için thread durduruldu.")


def main():
    config = load_config()

    postgres_client = PostgresClient(config)
    telemetry_generator = TelemetryGenerator(config['telemetry_fields'])
    command_handler = CommandHandler(postgres_client)

    influx_client = InfluxDBClient(
        url=config['influxdb']['url'],
        token=config['influxdb']['token'],
        org=config['influxdb']['org']
    )
    write_api = influx_client.write_api(write_options=SYNCHRONOUS)
    bucket = config['influxdb']['bucket']
    org = config['influxdb']['org']

    interval = config['simulation']['interval_seconds']
    duration = config['simulation']['duration_seconds']
    device_refresh_interval = config['simulation']['device_refresh_interval']

    device_statuses = postgres_client.get_all_devices()

    if not device_statuses:
        logger.error("Hiç cihaz bulunamadı, simülasyon durduruluyor.")
        return

    device_threads = {}

    def start_device_thread(device_id):
        stop_event = threading.Event()
        t = threading.Thread(
            target=simulate_device,
            args=(device_id, stop_event, telemetry_generator, command_handler, write_api, bucket, org, interval)
        )
        device_threads[device_id] = (t, stop_event)
        t.start()
        logger.info(f"Cihaz {device_id} için yeni thread başlatıldı.")

    logger.info(f"Simülasyon başlatıldı. {duration} saniye boyunca çalışacak.")

    for device_id, status in device_statuses.items():
        if status == "ACTIVE":
            start_device_thread(device_id)
        else:
            logger.info(f"Cihaz {device_id} ACTIVE değil (durum: {status}), thread başlatılmadı.")

    start_time = time.time()
    last_refresh = time.time()

    while time.time() - start_time < duration:
        if time.time() - last_refresh >= device_refresh_interval:
            logger.info("Cihaz listesi kontrol ediliyor...")
            current_statuses = postgres_client.get_all_devices()
            existing_ids = set(device_threads.keys())

            for device_id, status in current_statuses.items():
                if status == "ACTIVE" and device_id not in existing_ids:
                    start_device_thread(device_id)
                elif status != "ACTIVE" and device_id in existing_ids:
                    _, stop_event = device_threads[device_id]
                    stop_event.set()
                    logger.info(f"Cihaz {device_id} artık ACTIVE değil (durum: {status}), durduruluyor.")
                    del device_threads[device_id]

            removed_ids = existing_ids - set(current_statuses.keys())
            for removed_id in removed_ids:
                _, stop_event = device_threads[removed_id]
                stop_event.set()
                logger.info(f"Cihaz {removed_id} silinmiş, durduruluyor.")
                del device_threads[removed_id]

            if not device_threads:
                logger.warning("Şu an hiç aktif cihaz yok, sistem beklemede...")

            last_refresh = time.time()

        time.sleep(1)

    logger.info("Süre doldu, tüm thread'lere durdurma sinyali gönderiliyor.")
    for device_id, (t, stop_event) in device_threads.items():
        stop_event.set()

    for device_id, (t, stop_event) in device_threads.items():
        t.join()

    influx_client.close()
    logger.info("Simülasyon tamamlandı.")


if __name__ == "__main__":
    main()