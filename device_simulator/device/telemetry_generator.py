import random
from influxdb_client import Point
from utils.logger import setup_logger

logger = setup_logger(__name__)


class TelemetryGenerator:
    def __init__(self, fields_config, postgres_client):
        self.fields_config = fields_config
        self.postgres_client = postgres_client

    def generate(self, device_id):
        point = Point("device_telemetry").tag("device_id", str(device_id))

        for field in self.fields_config:
            value = self._generate_value(field)
            point = point.field(field["name"], value)

            self._check_alarm(device_id, field, value)

        logger.info(f"Veri uretildi (device_id={device_id}): {point.to_line_protocol()}")
        return point

    def _generate_value(self, field):
        data_type = field.get("data_type", "FLOAT")

        if data_type == "INTEGER":
            return random.randint(int(field["min"]), int(field["max"]))
        elif data_type == "BOOLEAN":
            return random.choice([True, False])
        else:
            return round(random.uniform(field["min"], field["max"]), 2)

    def _check_alarm(self, device_id, field, value):
        threshold = field.get("threshold")

        if threshold is not None and isinstance(value, (int, float)) and value > threshold:
            logger.warning(f"Device_{device_id} - {field['name']} esik degeri asti! ({value}) Alarm state guncelleniyor...")
            self.postgres_client.update_alarm_state(field["id"], "ACTIVE")
        elif threshold is not None:
            self.postgres_client.update_alarm_state(field["id"], "INACTIVE")