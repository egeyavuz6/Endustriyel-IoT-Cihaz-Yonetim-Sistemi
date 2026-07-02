package com.argela.iot_device_management.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable;
import com.influxdb.query.FluxRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TelemetryService {

    private final InfluxDBClient influxDBClient;

    @Value("${influxdb.bucket}")
    private String bucket;

    @Value("${influxdb.org}")
    private String org;

    public TelemetryService(InfluxDBClient influxDBClient) {
        this.influxDBClient = influxDBClient;
    }

    public List<Map<String, Object>> getTelemetryByDeviceId(Long deviceId) {
        String flux = String.format(
                "from(bucket: \"%s\") " +
                        "|> range(start: -1h) " +
                        "|> filter(fn: (r) => r.device_id == \"%s\")",
                bucket, deviceId
        );
        return executeQuery(flux);
    }

    public List<Map<String, Object>> getLatestTelemetry(Long deviceId) {
        String flux = String.format(
                "from(bucket: \"%s\") " +
                        "|> range(start: -1h) " +
                        "|> filter(fn: (r) => r.device_id == \"%s\") " +
                        "|> last()",
                bucket, deviceId
        );
        return executeQuery(flux);
    }

    public Map<String, Object> getTelemetryStats(Long deviceId) {
        Map<String, Object> stats = new HashMap<>();
        String [] fields = {"temperature", "humidity", "pressure", "vibration"};
        String [] aggregations = {"mean", "max", "min", "count"};

        for (String field : fields) {
            Map<String, Object> fieldStats = new HashMap<>();
            for (String aggregation : aggregations) {
                fieldStats.put(aggregation, getAggregatedValue(deviceId, field, aggregation));
            }
            stats.put(field, fieldStats);
        }
        return stats;
    }

    private Object getAggregatedValue(Long deviceId, String field, String aggregationFunction) {
        String flux = String.format(
                "from(bucket: \"%s\") " +
                        "|> range(start: -1h) " +
                        "|> filter(fn: (r) => r.device_id == \"%s\") " +
                        "|> filter(fn: (r) => r._field == \"%s\") " +
                        "|> %s()",
                bucket, deviceId, field, aggregationFunction
        );

        List<Map<String, Object>> result = executeQuery(flux);
        return result.isEmpty() ? null : result.get(0).get("value");
    }

    public void writeTelemetry(Long deviceId, double temperature, double humidity, double pressure, double vibration) {
        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();

        Point point = Point.measurement(    "device_telemetry")
                .addTag("device_id", String.valueOf(deviceId))
                .addField("temperature", temperature)
                .addField("humidity", humidity)
                .addField("pressure", pressure)
                .addField("vibration", vibration)
                .time(java.time.Instant.now(), WritePrecision.NS);

        writeApi.writePoint(bucket, org, point);
    }

    private List<Map<String, Object>> executeQuery(String flux) {
        QueryApi queryApi = influxDBClient.getQueryApi();
        List<FluxTable> tables = queryApi.query(flux, org);

        List<Map<String, Object>> results = new ArrayList<>();
        for (FluxTable table : tables) {
            for (FluxRecord record : table.getRecords()) {
                Map<String, Object> row = new HashMap<>();
                row.put("time", record.getTime());
                row.put("field", record.getField());
                row.put("value", record.getValue());
                results.add(row);
            }
        }
        return results;
    }
}