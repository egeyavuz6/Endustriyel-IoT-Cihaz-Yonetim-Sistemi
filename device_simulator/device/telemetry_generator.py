from dataclasses import field
import random
from influxdb_client import Point
from utils.logger import setup_logger

logger = setup_logger(__name__)


class TelemetryGenerator:
    def __init__(self, fields_config, postgres_client):
        self.fields_config = fields_config
        self.postgres_client = postgres_client

    def generate(self, device_id):
        operational_state = self.postgres_client.get_operational_state(device_id)

        if operational_state == "STOPPED":
            logger.info(f"Cihaz {device_id} STOPPED durumunda, veri uretilmiyor.")
            return None

        point = Point("device_telemetry").tag("device_id", str(device_id))

        for field in self.fields_config:
            value = self._generate_value(field, device_id)
            point = point.field(field["name"], value)
            self._check_alarm(device_id, field, value)

        logger.info(f"Veri uretildi (device_id={device_id}): {point.to_line_protocol()}")
        return point

    def _generate_value(self, field, device_id=None):
        data_type = field.get("data_type", "FLOAT")

        if field["name"] == "temperature" and device_id is not None:
            operational_state = self.postgres_client.get_operational_state(device_id)

            if operational_state == "RUNNING":
                target = self.postgres_client.get_latest_command_value(device_id, "SET_TEMPERATURE")
                if target is not None:
                    return round(target + random.uniform(-0.5, 0.5), 2)

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