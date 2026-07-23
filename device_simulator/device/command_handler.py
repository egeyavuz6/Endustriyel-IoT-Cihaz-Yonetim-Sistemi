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

        if command_type == "START":
            self.postgres_client.update_device_status(device_id, "ACTIVE")
            logger.info(f"Cihaz {device_id} baslatildi, durum: ACTIVE")
        elif command_type == "STOP":
            self.postgres_client.update_device_status(device_id, "PASSIVE")
            logger.info(f"Cihaz {device_id} durduruldu, durum: PASSIVE")
        else:
            logger.info(f"Bilinmeyen komut tipi: {command_type}")

        self.postgres_client.mark_command_executed(log_id)