package com.runnity.global.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "SPRING_PROFILES_ACTIVE", havingValue = "prod")
public class AwsS3Config {
    @Value("${AWS_ACCESS_KEY:AWS_ACCESS_KEY}") private String accessKey;
    @Value("${AWS_SECRET_KEY:AWS_SECRET_KEY}") private String secretKey;
    @Value("${AWS_STATIC:AWS_STATIC}") private String region;

    @Bean
    public AmazonS3 amazonS3() {
        BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(creds))
                .build();
    }
}