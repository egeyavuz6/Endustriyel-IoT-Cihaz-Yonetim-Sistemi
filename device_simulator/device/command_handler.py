from utils.logger import setup_logger

logger = setup_logger(__name__)


class CommandHandler:
    def __init__(self, api_client):
        self.api_client = api_client

    def check_and_execute_commands(self, device_id):
        pending_commands = self.api_client.get_pending_commands(device_id)

        for command in pending_commands:
            self.execute_command(device_id, command)

    def execute_command(self, device_id, command):
        command_type = command.get("commandType")
        command_id = command.get("id")

        logger.info(f"Komut alındı (device_id={device_id}): {command_type}")

        if command_type == "START":
            logger.info(f"Cihaz {device_id} başlatılıyor...")
        elif command_type == "STOP":
            logger.info(f"Cihaz {device_id} durduruluyor...")
        elif command_type == "RESET":
            logger.info(f"Cihaz {device_id} sıfırlanıyor...")
        else:
            logger.info(f"Bilinmeyen komut tipi: {command_type}")

        self.api_client.mark_command_executed(command_id)