package com.circle.backendprogrammering;

import org.springframework.boot.SpringApplication;

public class TestBackendProgrammeringApplication {

    public static void main(String[] args) {
        SpringApplication.from(BackendProgrammeringApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
