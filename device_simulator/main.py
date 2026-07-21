import time
import threading
from utils.config_loader import load_config
from utils.logger import setup_logger
from auth.keycloak_client import KeycloakClient
from api.api_client import ApiClient
from device.telemetry_generator import TelemetryGenerator
from device.command_handler import CommandHandler
from influxdb_client import InfluxDBClient
from influxdb_client.client.write_api import SYNCHRONOUS

logger = setup_logger(__name__)


def simulate_device(device_id, telemetry_generator, command_handler, write_api, bucket, org, interval, duration):
    start_time = time.time()
    while time.time() - start_time < duration:
        point = telemetry_generator.generate(device_id)
        write_api.write(bucket=bucket, org=org, record=point)
        command_handler.check_and_execute_commands(device_id)
        time.sleep(interval)
    logger.info(f"Cihaz {device_id} simülasyonu tamamlandı.")


def main():
    config = load_config()

    keycloak_client = KeycloakClient(config)
    token = keycloak_client.get_access_token()

    api_client = ApiClient(config, token)
    telemetry_generator = TelemetryGenerator()
    command_handler = CommandHandler(api_client)

    influx_client = InfluxDBClient(
        url=config['influxdb']['url'],
        token=config['influxdb']['token'],
        org=config['influxdb']['org']
    )
    write_api = influx_client.write_api(write_options=SYNCHRONOUS)
    bucket = config['influxdb']['bucket']
    org = config['influxdb']['org']

    device_ids = api_client.get_all_devices()

    if not device_ids:
        logger.error("Hiç cihaz bulunamadı, simülasyon durduruluyor.")
        return

    interval = config['simulation']['interval_seconds']
    duration = config['simulation']['duration_seconds']

    logger.info(f"Simülasyon başlatıldı. {len(device_ids)} cihaz, {duration} saniye boyunca paralel çalışacak.")

    threads = []
    for device_id in device_ids:
        t = threading.Thread(
            target=simulate_device,
            args=(device_id, telemetry_generator, command_handler, write_api, bucket, org, interval, duration)
        )
        threads.append(t)
        t.start()

    for t in threads:
        t.join()

    influx_client.close()
    logger.info("Tüm cihazların simülasyonu tamamlandı.")


if __name__ == "__main__":
    main()