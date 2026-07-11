package com.oprica.tmsapi;

import com.oprica.tmsapi.properties.TmsProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(TmsProperties.class)
@SpringBootApplication
public class TmsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TmsApiApplication.class, args);
    }
}
