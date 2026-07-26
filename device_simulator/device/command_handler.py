from utils.logger import setup_logger

logger = setup_logger(__name__)


class CommandHandler:
    def __init__(self, postgres_client):
        self.postgres_client = postgres_client

    def check_and_execute_commands(self, device_id):
        pending_commands = self.postgres_client.get_pending_commands(device_id)

        for command in pending_commands:
            self.execute_command(device_id, command)

    def execute_command(self, device_id, command):
        command_type = command.get("command_type")
        log_id = command.get("id")

        logger.info(f"Komut alindi (device_id={device_id}): {command_type}")

        device_status = self.postgres_client.get_device_status(device_id)

        if command_type == "POWER_ON":
            self.postgres_client.update_device_status(device_id, "ACTIVE")
            logger.info(f"Cihaz {device_id} baslatildi, durum: ACTIVE")

        elif command_type == "POWER_OFF":
            self.postgres_client.update_device_status(device_id, "PASSIVE")
            self.postgres_client.update_operational_state(device_id, "STOPPED")
            logger.info(f"Cihaz {device_id} durduruldu, durum: PASSIVE, operational_state: STOPPED")

        elif command_type == "START":
            if device_status != "ACTIVE":
                logger.warning(f"Cihaz {device_id} PASSIVE durumda, START komutu yoksayildi.")
            else:
                self.postgres_client.update_operational_state(device_id, "RUNNING")
                logger.info(f"Cihaz {device_id} fonksiyonu baslatildi (RUNNING)")

        elif command_type == "STOP":
            self.postgres_client.update_operational_state(device_id, "STOPPED")
            logger.info(f"Cihaz {device_id} fonksiyonu durduruldu (STOPPED)")

        elif command_type == "SET_TEMPERATURE":
            if device_status != "ACTIVE":
                logger.warning(f"Cihaz {device_id} PASSIVE durumda, SET_TEMPERATURE komutu yoksayildi.")
            else:
                logger.info(f"Cihaz {device_id} icin sicaklik ayari alindi.")

        else:
            logger.info(f"Bilinmeyen komut tipi: {command_type}")

        self.postgres_client.mark_command_executed(log_id)