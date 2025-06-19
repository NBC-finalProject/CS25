package com.example.cs25entity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = {
                "com.example"
        }
)
public class Cs25EntityApplication {

    public static void main(String[] args) {
        SpringApplication.run(Cs25EntityApplication.class, args);
    }

}
