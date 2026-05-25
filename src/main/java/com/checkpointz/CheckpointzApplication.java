package com.checkpointz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CheckpointzApplication {

    public static void main(String[] args) {
        SpringApplication.run(CheckpointzApplication.class, args);
    }
}