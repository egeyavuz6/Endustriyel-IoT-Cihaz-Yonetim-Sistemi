import random
from influxdb_client import Point
from utils.logger import setup_logger

logger = setup_logger(__name__)


class TelemetryGenerator:
    def __init__(self, fields_config):
        self.fields_config = fields_config

    def generate(self, device_id):
        point = Point("device_telemetry").tag("device_id", str(device_id))

        for field in self.fields_config:
            value = round(random.uniform(field["min"], field["max"]), 2)
            point = point.field(field["name"], value)

        logger.info(f"Veri üretildi (device_id={device_id}): {point.to_line_protocol()}")
        return point