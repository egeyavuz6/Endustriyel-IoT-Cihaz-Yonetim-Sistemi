package com.argela.iot_device_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IotDeviceManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(IotDeviceManagementApplication.class, args);
	}

}
