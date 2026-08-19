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
            return None

        if len(self.fields_config) == 0:
            logger.warning(f"{device_id} numarali id ye sahip cihaz icin aktif veri bulunamadi. Telemetri uretilemiyor.")
            return None

        point = Point("device_telemetry").tag("device_id", str(device_id))

        for field in self.fields_config:
            value = self._generate_value(field, device_id)
            point = point.field(field["name"], value)
            self._check_alarm(device_id, field, value)

        self.postgres_client.update_last_seen(device_id)  

        logger.info(f"Veri uretildi (device_id={device_id}): {point.to_line_protocol()}")
        return point

    def _generate_value(self, field, device_id=None):
        data_type = field.get("data_type", "FLOAT")

        if field["name"] == "temperature" and device_id is not None:
            operational_state = self.postgres_client.get_operational_state(device_id)

            if operational_state == "RUNNING":
                target = self.postgres_client.get_latest_command_value(device_id, "SET_TEMPERATURE")
                if target is not None:
                    return round(target + random.uniform(-0.1, 0.1), 2)

        if data_type == "INTEGER":
            return random.randint(int(field["min"]), int(field["max"]))
        elif data_type == "BOOLEAN":
            return random.choice([True, False])
        elif data_type == "STRING":
            possible = field.get("possible_values")
            if possible:
                return random.choice(possible.split(","))
            return "UNKNOWN"
        else:
            return round(random.uniform(field["min"], field["max"]), 2)

    def _check_alarm(self, device_id, field, value):
        alarm_enabled = field.get("alarm_enabled", False)
        check_type = field.get("alarm_check_type", "OUT_OF_RANGE")
        previous_state = field.get("alarm_state")
        data_type = field.get("data_type", "FLOAT")

        if not alarm_enabled:
            return

        min_raw = field.get("alarm_min_threshold")
        max_raw = field.get("alarm_max_threshold")

        def cast(raw):
            if raw is None:
                return None
            if data_type == "STRING":
                return str(raw)
            try:
                return float(raw)
            except (ValueError, TypeError):
                return None

        min_t = cast(min_raw)
        max_t = cast(max_raw)
        compare_value = str(value) if data_type == "STRING" else value

        is_alarm = False

        if check_type == "OUT_OF_RANGE":
            if min_t is not None and compare_value < min_t:
                is_alarm = True
            if max_t is not None and compare_value > max_t:
                is_alarm = True

        elif check_type == "IN_RANGE":
            if min_t is not None and max_t is not None:
                is_alarm = min_t <= compare_value <= max_t
            elif min_t is not None:
                is_alarm = compare_value >= min_t
            elif max_t is not None:
                is_alarm = compare_value <= max_t

        new_state = "ACTIVE" if is_alarm else "INACTIVE"

        if new_state != previous_state:
            logger.warning(f"Device_{device_id} - {field['name']} durumu degisti: {previous_state} -> {new_state} ({value}, tip: {check_type})")
            if new_state == "ACTIVE":
                self.postgres_client.open_alarm(device_id, field["id"], value)
            else:
                self.postgres_client.close_alarm(device_id, field["id"], value)
            field["alarm_state"] = new_state

        self.postgres_client.update_alarm_state(field["id"], new_state)
        self.postgres_client.upsert_device_alarm(device_id, field["id"], value, new_state)