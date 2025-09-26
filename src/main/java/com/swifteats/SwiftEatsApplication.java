package com.swifteats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SwiftEatsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SwiftEatsApplication.class, args);
    }
}


