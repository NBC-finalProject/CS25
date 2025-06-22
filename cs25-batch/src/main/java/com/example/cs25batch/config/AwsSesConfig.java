package com.example.cs25batch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

@Configuration
public class AwsSesConfig {
    private String accessKey;
    private String secretKey;
    private String region;

    @Bean
    public SesV2Client amazonSesClient() {	// SES V2 사용 시 SesV2Client
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        return SesV2Client.builder()
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .region(Region.of(region))
            .build();
    }
}
