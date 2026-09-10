package com.enterprise.platformhealthservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = "com.enterprise")
@EnableDiscoveryClient
public class SuperAdminApplication {

    public static void main(String[] args) {

        SpringApplication.run(SuperAdminApplication.class, args);
    }

}
