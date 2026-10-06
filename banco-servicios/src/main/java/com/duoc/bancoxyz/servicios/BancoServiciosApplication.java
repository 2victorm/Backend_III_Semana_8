package com.duoc.bancoxyz.servicios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BancoServiciosApplication {

    public static void main(String[] args) {
        SpringApplication.run(BancoServiciosApplication.class, args);
    }
}
