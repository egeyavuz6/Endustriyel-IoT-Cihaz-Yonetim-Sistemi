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
        if point is not None:
            write_api.write(bucket=bucket, org=org, record=point)
        command_handler.check_and_execute_commands(device_id)
        time.sleep(interval)
    logger.info(f"Cihaz {device_id} icin thread durduruldu.")


def main():
    config = load_config()

    postgres_client = PostgresClient(config)
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

    devices = postgres_client.get_all_devices()

    if not devices:
        logger.error("Hic cihaz bulunamadi, simulasyon durduruluyor.")
        return

    device_threads = {}
    device_generators = {}

    def start_device_thread(device_id, device_type):
        telemetry_fields = postgres_client.get_telemetry_fields(device_id)  
        generator = TelemetryGenerator(telemetry_fields, postgres_client)
    
        device_generators[device_id] = generator

        stop_event = threading.Event()
        t = threading.Thread(
            target=simulate_device,
            args=(device_id, stop_event, generator, command_handler, write_api, bucket, org, interval)
        )
        device_threads[device_id] = (t, stop_event)
        t.start()
        logger.info(f"Cihaz {device_id} (tip: {device_type}) icin yeni thread baslatildi.")

    logger.info(f"Simulasyon baslatildi. {duration} saniye boyunca calisacak.")

    passive_count = 0
    active_count = 0

    for device_id, info in devices.items():
        if info["status"] == "ACTIVE":
            start_device_thread(device_id, info["type"])
            active_count += 1
        else:
            passive_count += 1

    logger.info(f"{active_count} cihaz baslatildi, {passive_count} cihaz PASSIVE oldugu icin baslatilmadi.")

    start_time = time.time()
    last_refresh = time.time()

    while time.time() - start_time < duration:
        if time.time() - last_refresh >= device_refresh_interval:
            # logger.info("Cihaz listesi kontrol ediliyor...")

            postgres_client.check_and_update_offline_devices()
            current_devices = postgres_client.get_all_devices()
            existing_ids = set(device_threads.keys())

            for device_id in current_devices.keys():
                command_handler.check_and_execute_commands(device_id)

            for device_id, info in current_devices.items():
                if device_id in device_generators:
                    fresh_fields = postgres_client.get_telemetry_fields(device_id)  # device_id
                    device_generators[device_id].fields_config = fresh_fields

            for device_id, info in current_devices.items():
                if info["status"] == "ACTIVE" and device_id not in existing_ids:
                    start_device_thread(device_id, info["type"])
                elif info["status"] != "ACTIVE" and device_id in existing_ids:
                    _, stop_event = device_threads[device_id]
                    stop_event.set()
                    logger.info(f"Cihaz {device_id} artik ACTIVE degil (durum: {info['status']}), durduruluyor.")
                    del device_threads[device_id]
                    del device_generators[device_id]

            removed_ids = existing_ids - set(current_devices.keys())
            for removed_id in removed_ids:
                _, stop_event = device_threads[removed_id]
                stop_event.set()
                logger.info(f"Cihaz {removed_id} silinmis, durduruluyor.")
                del device_threads[removed_id]
                del device_generators[removed_id]

            if not device_threads:
                logger.warning("Su an hic aktif cihaz yok, sistem beklemede...")

            last_refresh = time.time()

    time.sleep(1)


    for device_id, (t, stop_event) in device_threads.items():
        stop_event.set()

    for device_id, (t, stop_event) in device_threads.items():
        t.join()

    influx_client.close()
    logger.info("Simulasyon tamamlandi.")


if __name__ == "__main__":
    main()