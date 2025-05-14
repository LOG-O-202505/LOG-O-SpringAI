package com.ssafy.logoserver;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition
public class LogoServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogoServerApplication.class, args);
    }

}