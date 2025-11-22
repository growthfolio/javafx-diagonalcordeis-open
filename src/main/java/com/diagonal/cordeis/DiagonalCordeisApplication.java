package com.diagonal.cordeis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan
@org.springframework.scheduling.annotation.EnableScheduling
@org.springframework.scheduling.annotation.EnableAsync
public class DiagonalCordeisApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiagonalCordeisApplication.class, args);
    }

}

