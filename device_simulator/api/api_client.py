import requests
from utils.logger import setup_logger

logger = setup_logger(__name__)


class ApiClient:
    def __init__(self, config, token):
        self.base_url = config['api']['base_url']
        self.token = token

    def get_headers(self):
        return {"Authorization": f"Bearer {self.token}"}

    def get_all_devices(self):
        try:
            url = f"{self.base_url}/api/devices"
            response = requests.get(url, headers=self.get_headers())
            response.raise_for_status()
            devices = response.json()
            active_devices = {
                device["id"]: device["status"] 
                for device in devices}
            logger.info(f"Spring Boot'tan {len(active_devices)} cihaz bulundu.")
            return active_devices
        except Exception as e:
            logger.error(f"Cihaz listesi alınamadı: {e}")

    def get_pending_commands(self, device_id):
        url = f"{self.base_url}/api/devices/{device_id}/commands"
        response = requests.get(url, headers=self.get_headers())
        response.raise_for_status()
        all_commands = response.json()
        return [c for c in all_commands if c.get("status") == "PENDING"]

    def mark_command_executed(self, command_id):
        url = f"{self.base_url}/api/commands/{command_id}"
        payload = {"status": "EXECUTED"}
        requests.put(url, json=payload, headers=self.get_headers())        