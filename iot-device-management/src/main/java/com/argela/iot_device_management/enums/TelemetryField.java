package com.argela.iot_device_management.enums;

public enum TelemetryField {
    TEMPERATURE(1, "temperature"),
    HUMIDITY(2, "humidity"),
    PRESSURE(3, "pressure"),
    VIBRATION(4, "vibration");

    private final int id;
    private final String fieldName;

    TelemetryField(int id, String fieldName) {
        this.id = id;
        this.fieldName = fieldName;
    }

    public int getId() {
        return id;
    }

    public String getFieldName() {
        return fieldName;
    }

    public static TelemetryField fromId(int id) {
        for (TelemetryField field : values()) {
            if (field.id == id) {
                return field;
            }
        }
        throw new IllegalArgumentException("Geçersiz field id: " + id);
    }
}