import requests
from utils.logger import setup_logger

logger = setup_logger(__name__)


class KeycloakClient:
    def __init__(self, config):
        self.token_url = f"{config['api']['keycloak_url']}/realms/{config['api']['realm']}/protocol/openid-connect/token"
        self.client_id = config['api']['client_id']
        self.client_secret = config['api']['client_secret']
        self.username = config['api']['username']
        self.password = config['api']['password']

    def get_access_token(self):
        data = {
            "client_id": self.client_id,
            "client_secret": self.client_secret,
            "grant_type": "client_credentials"
        }
        response = requests.post(self.token_url, data=data)
        response.raise_for_status()
        token = response.json()["access_token"]
        logger.info("Keycloak'tan token alındı (Client Credentials).")
        return token