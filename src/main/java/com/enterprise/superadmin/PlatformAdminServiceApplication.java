package com.enterprise.superadmin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class PlatformAdminServiceApplication {

<<<<<<< HEAD:src/main/java/com/enterprise/superadmin/PlatformAdminServiceApplication.java
	public static void main(String[] args) {
		SpringApplication.run(PlatformAdminServiceApplication.class, args);
	}
=======
    private static final Logger log = LoggerFactory.getLogger(SuperAdminApplication.class);

    public static void main(String[] args) {
        log.info("Starting Super Admin Management Service...");
        SpringApplication.run(SuperAdminApplication.class, args);
        log.info("Super Admin Management Service started successfully.");
    }
>>>>>>> origin/main:src/main/java/com/enterprise/superadmin/SuperAdminApplication.java

}