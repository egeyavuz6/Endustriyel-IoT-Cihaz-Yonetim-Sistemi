import random
from influxdb_client import Point
from utils.logger import setup_logger

logger = setup_logger(__name__)


class TelemetryGenerator:
    def generate(self, device_id):
        point = (
            Point("device_telemetry")
            .tag("device_id", str(device_id))
            .field("temperature", round(random.uniform(15.0, 35.0), 2))
            .field("humidity", round(random.uniform(30.0, 70.0), 2))
            .field("pressure", round(random.uniform(1000.0, 1020.0), 2))
            .field("vibration", round(random.uniform(0.0, 5.0), 2))
        )
        logger.info(f"Veri üretildi (device_id={device_id}): {point.to_line_protocol()}")
        return point