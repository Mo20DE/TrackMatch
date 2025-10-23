package com.trackmatch.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EnableAsync
public class ServerApplication {

	public static void main(String[] args) {
        Dotenv dotenv = Dotenv.load();
        dotenv.entries().forEach((entry) -> {
            String key = entry.getKey();
            String value = entry.getValue();
            System.setProperty(key, value);
        });
        SpringApplication.run(ServerApplication.class, args);
	}
}
