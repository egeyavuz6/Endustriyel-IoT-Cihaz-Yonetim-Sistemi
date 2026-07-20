import time
from utils.config_loader import load_config
from utils.logger import setup_logger
from auth.keycloak_client import KeycloakClient
from api.api_client import ApiClient
from device.telemetry_generator import TelemetryGenerator
from device.command_handler import CommandHandler
from influxdb_client import InfluxDBClient
from influxdb_client.client.write_api import SYNCHRONOUS

logger = setup_logger(__name__)


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
    batch_size = config['simulation']['batch_write_size']

    logger.info(f"Simülasyon başlatıldı. {duration} saniye boyunca çalışacak.")

    start_time = time.time()
    points_batch = []

    while time.time() - start_time < duration:
        for device_id in device_ids:
            point = telemetry_generator.generate(device_id)
            points_batch.append(point)

            command_handler.check_and_execute_commands(device_id)

        if len(points_batch) >= batch_size:
            write_api.write(bucket=bucket, org=org, record=points_batch)
            logger.info(f"{len(points_batch)} veri noktası yazıldı.")
            points_batch = []

        time.sleep(interval)

    if points_batch:
        write_api.write(bucket=bucket, org=org, record=points_batch)

    influx_client.close()
    logger.info("Simülasyon tamamlandı.")


if __name__ == "__main__":
    main()