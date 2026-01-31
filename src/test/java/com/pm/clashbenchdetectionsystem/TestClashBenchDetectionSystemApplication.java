package com.pm.clashbenchdetectionsystem;

import org.springframework.boot.SpringApplication;

public class TestClashBenchDetectionSystemApplication {

    public static void main(String[] args) {
        SpringApplication.from(ClashBenchDetectionSystemApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
