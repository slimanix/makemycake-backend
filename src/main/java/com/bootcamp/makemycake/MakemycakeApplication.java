package com.bootcamp.makemycake;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MakemycakeApplication {

    public static void main(String[] args) {
        // Force the active profile to 'prod'
        System.setProperty("spring.profiles.active", "prod");
        SpringApplication.run(MakemycakeApplication.class, args);
    }

}
