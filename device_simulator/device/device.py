class Device:
    def __init__(self, device_id, serial_number=None, name=None):
        self.device_id = device_id
        self.serial_number = serial_number
        self.name = name
        self.is_active = True

    def __repr__(self):
        return f"Device(id={self.device_id}, name={self.name}, active={self.is_active})"