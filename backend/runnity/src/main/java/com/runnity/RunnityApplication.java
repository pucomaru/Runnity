package com.runnity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.runnity.broadcast.client")
@SpringBootApplication
public class RunnityApplication {

    public static void main(String[] args) {
        SpringApplication.run(RunnityApplication.class, args);
    }

}
