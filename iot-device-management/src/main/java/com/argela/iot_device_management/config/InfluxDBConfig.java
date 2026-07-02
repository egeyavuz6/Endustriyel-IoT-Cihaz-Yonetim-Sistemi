package com.argela.iot_device_management.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfluxDBConfig {


    @Value("${influxdb.url}")
    private String url;

    @Value("${influxdb.token}")
    private String token;

    @Value("${influxdb.org}")
    private String org;

    @Bean
    public InfluxDBClient influxDBClient() {
        //System.out.println("TOKEN: [" + token + "]");   doğru dönüyor mu diye kontrol
        //System.out.println("URL: [" + url + "]");
        //System.out.println("ORG: [" + org + "]");
        return InfluxDBClientFactory.create(url, token.toCharArray(), org);
    }
}