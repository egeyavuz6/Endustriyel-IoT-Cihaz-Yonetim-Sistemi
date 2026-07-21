import psycopg2
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
                    "SELECT id, command_type, command_value FROM device_commands "
                    "WHERE device_id = %s AND status = 'PENDING'",
                    (device_id,)
                )
                return cursor.fetchall()
        finally:
            conn.close()

    def mark_command_executed(self, command_id):
        conn = self.get_connection()
        try:
            with conn.cursor() as cursor:
                cursor.execute(
                    "UPDATE device_commands SET status = 'EXECUTED', executed_at = NOW() "
                    "WHERE id = %s",
                    (command_id,)
                )
                conn.commit()
                logger.info(f"Komut {command_id} EXECUTED olarak işaretlendi.")
        finally:
            conn.close()

    def get_telemetry_fields(self):
        conn = self.get_connection()
        try:
            with conn.cursor(cursor_factory=RealDictCursor) as cursor:
                cursor.execute("SELECT name, min_value, max_value FROM telemetry_fields")
                rows = cursor.fetchall()
                fields = [
                    {"name": row["name"], "min": row["min_value"], "max": row["max_value"]}
                    for row in rows
                ]
                return fields
        finally:
            conn.close()