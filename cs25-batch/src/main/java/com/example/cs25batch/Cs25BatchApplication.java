package com.example.cs25batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = {
                "com.example"
        }
)
public class Cs25BatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(Cs25BatchApplication.class, args);
    }

}
