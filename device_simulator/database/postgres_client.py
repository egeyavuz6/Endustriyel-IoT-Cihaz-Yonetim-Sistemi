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
                cursor.execute("SELECT id, status, type FROM devices")
                rows = cursor.fetchall()
                devices = {row['id']: {"status": row['status'], "type": row['type']} for row in rows}
                logger.info(f"PostgreSQL'den {len(devices)} cihaz bulundu.")
                return devices
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
                    "SELECT operational_state FROM devices WHERE id = %s",
                    (device_id,)
                )
                row = cursor.fetchone()
                return row["operational_state"] if row else None
        finally:
            conn.close()
    def get_telemetry_fields(self, device_type):
        conn = self.get_connection()
        try:
            with conn.cursor(cursor_factory=RealDictCursor) as cursor:
                cursor.execute(
                    "SELECT dc.id, dc.command_type, dc.min_value, dc.max_value, dc.data_type, dc.threshold_value "
                    "FROM device_commands dc "
                    "JOIN device_type_commands dtc ON dc.id = dtc.command_id "
                    "WHERE dtc.device_type = %s AND dc.operation_type = 'READ'",
                    (device_type,)
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
                    "UPDATE devices SET operational_state = %s WHERE id = %s",
                    (state, device_id)
                )
                conn.commit()
        finally:
            conn.close()