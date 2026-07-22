from utils.config_loader import load_config
from database.postgres_client import PostgresClient

config = load_config()
postgres_client = PostgresClient(config)

for i in range(1, 4):
    postgres_client.create_device(device_number=i)