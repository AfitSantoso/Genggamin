package com.example.genggamin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class GenggaminApplication {

    public static void main(String[] args) {
        SpringApplication.run(GenggaminApplication.class, args);
    }

}
