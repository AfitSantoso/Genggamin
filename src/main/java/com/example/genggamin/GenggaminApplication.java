package com.example.genggamin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling  // Enable scheduled tasks untuk cleanup token
public class GenggaminApplication {

    public static void main(String[] args) {
        SpringApplication.run(GenggaminApplication.class, args);
    }

}
