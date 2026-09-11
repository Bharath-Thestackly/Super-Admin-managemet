package com.enterprise.superadmin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class SuperAdminApplication {

    private static final Logger log = LoggerFactory.getLogger(SuperAdminApplication.class);

    public static void main(String[] args) {
        log.info("Starting Super Admin Management Service...");
        SpringApplication.run(SuperAdminApplication.class, args);
        log.info("Super Admin Management Service started successfully.");
    }

}