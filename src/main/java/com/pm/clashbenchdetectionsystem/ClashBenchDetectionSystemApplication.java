package com.pm.clashbenchdetectionsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class ClashBenchDetectionSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClashBenchDetectionSystemApplication.class, args);
    }
}
