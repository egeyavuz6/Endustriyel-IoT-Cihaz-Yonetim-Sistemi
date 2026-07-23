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
                cursor.execute("SELECT id, status FROM devices")
                rows = cursor.fetchall()
                device_statuses = {row['id']: row['status'] for row in rows}
                return device_statuses
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

    def get_telemetry_fields(self):
        conn = self.get_connection()
        try:
            with conn.cursor(cursor_factory=RealDictCursor) as cursor:
                cursor.execute(
                    "SELECT id, command_type, min_value, max_value, data_type, threshold_value "
                    "FROM device_commands WHERE operation_type = 'READ'"
                )
                rows = cursor.fetchall()
                fields = [
                    {
                        "id": row["id"],
                        "name": row["command_type"].lower(),
                        "min": row["min_value"],
                        "max": row["max_value"],
                        "data_type": row["data_type"],
                        "threshold": row["threshold_value"]
                    }
                    for row in rows
                ]
                logger.info(f"PostgreSQL'den {len(fields)} telemetry field bulundu: {[f['name'] for f in fields]}")
                return fields
        finally:
            conn.close()

    def update_device_status(self, device_id, new_status):
        conn = self.get_connection()
        try:
            with conn.cursor() as cursor:
                cursor.execute(
                    "UPDATE devices SET status = %s WHERE id = %s",
                    (new_status, device_id)
                )
                conn.commit()
                logger.info(f"Cihaz {device_id} durumu güncellendi: {new_status}")
        except Exception as e:
            conn.rollback()
            logger.error(f"Cihaz durumu güncellenemedi: {e}")
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