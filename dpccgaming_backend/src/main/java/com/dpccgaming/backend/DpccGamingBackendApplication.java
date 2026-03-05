package com.dpccgaming.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.dpccgaming.backend.auth.repository")
public class DpccGamingBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(DpccGamingBackendApplication.class, args);
    }
}
