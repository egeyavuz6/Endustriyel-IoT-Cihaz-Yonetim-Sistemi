import psycopg2
import uuid
import random
from psycopg2.extras import RealDictCursor
from utils.logger import setup_logger

logger = setup_logger(__name__)


class PostgresClient:
    def __init__(self, config):
        self.config = config['postgres']

    def get_connection(self):
        return psycopg2.connect(
            host=self.config['host'],
            port=self.config['port'],
            dbname=self.config['database'],
            user=self.config['user'],
            password=self.config['password']
        )

    def get_all_devices(self):
        conn = self.get_connection()
        try:
            with conn.cursor(cursor_factory=RealDictCursor) as cursor:
                cursor.execute(
                    "SELECT d.id, d.type, "
                    "COALESCE(dc.current_state, 'OFF') as power_state "
                    "FROM devices d "
                    "LEFT JOIN device_commands dc ON dc.device_id = d.id "
                    "AND dc.command_type = 'POWER_ON' AND dc.operation_type = 'R/W'"
                )
                rows = cursor.fetchall()
                devices = {
                    row['id']: {
                        "status": "ACTIVE" if row['power_state'] == 'ON' else "PASSIVE",
                        "type": row['type']
                    }
                    for row in rows
                }
                logger.info(f"PostgreSQL'den {len(devices)} cihaz bulundu.")
                return devices
        finally:
            conn.close()
    
    def get_device_status(self, device_id):
        conn = self.get_connection()
        try:
            with conn.cursor(cursor_factory=RealDictCursor) as cursor:
                cursor.execute(
                    "SELECT current_state FROM device_commands "
                    "WHERE device_id = %s AND command_type = 'POWER_ON' AND operation_type = 'R/W'",
                    (device_id,)
                )
                row = cursor.fetchone()
                return "ACTIVE" if row and row["current_state"] == "ON" else "PASSIVE"
        finally:
            conn.close()


    def update_device_status(self, device_id, status):
        state = "ON" if status == "ACTIVE" else "OFF"
        conn = self.get_connection()
        try:
            with conn.cursor() as cursor:
                cursor.execute(
                    "UPDATE device_commands SET current_state = %s "
                    "WHERE device_id = %s AND command_type = 'POWER_ON' AND operation_type = 'R/W'",
                    (state, device_id)
                )
                if status == "ACTIVE":
                    cursor.execute(
                        "UPDATE devices SET last_seen_at = NOW(), connection_status = 'ONLINE' "
                        "WHERE id = %s",
                        (device_id,)
                    )
                conn.commit()
        finally:
            conn.close()


    def get_pending_commands(self, device_id):
        conn = self.get_connection()
        try:
            with conn.cursor(cursor_factory=RealDictCursor) as cursor:
                cursor.execute(
                    "SELECT cl.id, dc.command_type "
                    "FROM command_logs cl "
                    "JOIN device_commands dc ON cl.command_id = dc.id "
                    "WHERE cl.device_id = %s AND cl.status = 'PENDING'",
                    (device_id,)
                )
                return cursor.fetchall()
        finally:
            conn.close()

    def mark_command_executed(self, log_id):
        conn = self.get_connection()
        try:
            with conn.cursor() as cursor:
                cursor.execute(
                    "UPDATE command_logs SET status = 'EXECUTED', executed_at = NOW() "
                    "WHERE id = %s",
                    (log_id,)
                )
                conn.commit()
                logger.info(f"Komut logu {log_id} EXECUTED olarak isaretlendi.")
        finally:
            conn.close()


    def get_operational_state(self, device_id):
        conn = self.get_connection()
        try:
            with conn.cursor(cursor_factory=RealDictCursor) as cursor:
                cursor.execute(
                    "SELECT current_state FROM device_commands "
                    "WHERE device_id = %s AND command_type = 'START' AND operation_type = 'R/W'",
                    (device_id,)
                )
                row = cursor.fetchone()
                return row["current_state"] if row and row["current_state"] else "STOPPED"
        finally:
            conn.close()


    def get_telemetry_fields(self, device_id):
        conn = self.get_connection()
        try:
            with conn.cursor(cursor_factory=RealDictCursor) as cursor:
                cursor.execute(
                    "SELECT id, command_type, min_value, max_value, data_type, "
                    "threshold_value, alarm_state, alarm_enabled "
                    "FROM device_commands "
                    "WHERE device_id = %s AND operation_type = 'READ' AND is_active = TRUE",
                    (device_id,)
                )
                rows = cursor.fetchall()
                fields = [
                    {
                        "id": row["id"],
                        "name": row["command_type"].lower(),
                        "min": row["min_value"],
                        "max": row["max_value"],
                        "data_type": row["data_type"],
                        "threshold": row["threshold_value"],
                        "alarm_state": row["alarm_state"],
                        "alarm_enabled": row["alarm_enabled"]
                    }
                    for row in rows
                ]
                return fields
        finally:
            conn.close()

    

    def update_alarm_state(self, command_id, alarm_state):
        conn = self.get_connection()
        try:
            with conn.cursor() as cursor:
                cursor.execute(
                    "UPDATE device_commands SET alarm_state = %s WHERE id = %s",
                    (alarm_state, command_id)
                )
                conn.commit()
        finally:
            conn.close()
            
    def create_device(self, device_type="SENSOR", status="ACTIVE"):
        serial_number = f"SN{uuid.uuid4().hex[:8].upper()}"

        locations = ["Fabrika A - Hat 1", "Fabrika A - Hat 2", "Fabrika B - Hat 1",
                    "Fabrika B - Hat 2", "Fabrika C - Hat 1"]
        location = random.choice(locations)

        conn = self.get_connection()
        try:
            with conn.cursor(cursor_factory=RealDictCursor) as cursor:
                cursor.execute(
                    "INSERT INTO devices (name, serial_number, type, location, status, created_at) "
                    "VALUES (%s, %s, %s, %s, %s, NOW()) "
                    "RETURNING id",
                    ("TEMP", serial_number, device_type, location, status)
                )
                new_id = cursor.fetchone()["id"]

                final_name = f"Sensor-{new_id}"
                cursor.execute(
                    "UPDATE devices SET name = %s WHERE id = %s",
                    (final_name, new_id)
                )

                conn.commit()
                logger.info(f"Yeni cihaz oluşturuldu: id={new_id}, name={final_name}, serial={serial_number}, location={location}")
                return new_id
        except Exception as e:
            conn.rollback()
            logger.error(f"Cihaz oluşturulamadı: {e}")
            return None
        finally:
            conn.close()

    def get_latest_command_value(self, device_id, command_type):
        conn = self.get_connection()
        try:
            with conn.cursor(cursor_factory=RealDictCursor) as cursor:
                cursor.execute(
                    "SELECT cl.command_value "
                    "FROM command_logs cl "
                    "JOIN device_commands dc ON cl.command_id = dc.id "
                    "WHERE cl.device_id = %s AND dc.command_type = %s "
                    "ORDER BY cl.created_at DESC LIMIT 1",
                    (device_id, command_type)
                )
                row = cursor.fetchone()
                return float(row["command_value"]) if row and row["command_value"] else None
        finally:
            conn.close()

    def update_operational_state(self, device_id, state):
        conn = self.get_connection()
        try:
            with conn.cursor() as cursor:
                cursor.execute(
                    "UPDATE device_commands SET current_state = %s "
                    "WHERE device_id = %s AND command_type = 'START' AND operation_type = 'R/W'",
                    (state, device_id)
                )
                conn.commit()
        finally:
            conn.close()

    def upsert_device_alarm(self, device_id, command_id, value, alarm_state):
        conn = self.get_connection()
        try:
            with conn.cursor() as cursor:
                cursor.execute(
                    "INSERT INTO notifications (device_id, command_id, current_value, alarm_state, updated_at) "
                    "VALUES (%s, %s, %s, %s, NOW()) "
                    "ON CONFLICT (device_id, command_id) "
                    "DO UPDATE SET current_value = %s, alarm_state = %s, updated_at = NOW()",
                    (device_id, command_id, value, alarm_state, value, alarm_state)
                )
                conn.commit()
        finally:
            conn.close()

    def update_last_seen(self, device_id):
        conn = self.get_connection()
        try:
            with conn.cursor() as cursor:
                cursor.execute(
                    "UPDATE devices SET last_seen_at = NOW(), connection_status = 'ONLINE' "
                    "WHERE id = %s",
                    (device_id,)
                )
                conn.commit()
        finally:
            conn.close()

    def check_and_update_offline_devices(self):
        conn = self.get_connection()
        try:
            with conn.cursor() as cursor:
                cursor.execute(
                    "UPDATE devices d "
                    "SET connection_status = 'OFFLINE' "
                    "FROM device_types dt "
                    "WHERE d.type = dt.device_type "
                    "AND d.connection_status = 'ONLINE' "
                    "AND d.last_seen_at < NOW() - (dt.offline_threshold_minutes || ' minutes')::INTERVAL"
                )
                affected = cursor.rowcount
                conn.commit()
                if affected > 0:
                    logger.warning(f"{affected} cihaz OFFLINE olarak isaretlendi.")
        finally:
            conn.close()