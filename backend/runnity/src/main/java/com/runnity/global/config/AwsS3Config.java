package com.runnity.global.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Slf4j
@Configuration
public class AwsS3Config {

    @Value("${AWS_ACCESS_KEY:dummy}")
    private String accessKey;

    @Value("${AWS_SECRET_KEY:dummy}")
    private String secretKey;

    @Value("${AWS_REGION:ap-northeast-2}")
    private String region;

    @Bean
    @Profile("prod")  // prod 프로파일에서만 실제 S3 클라이언트 생성
    public AmazonS3 amazonS3Prod() {
        log.info("Creating real AmazonS3 client for production");
        BasicAWSCredentials creds = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(creds))
                .build();
    }

    @Bean
    @Profile("!prod")  // prod가 아닌 모든 프로파일(local, dev 등)
    public AmazonS3 amazonS3Mock() {
        log.info("Creating mock AmazonS3 client for non-production environment");
        // Mock 또는 Stub 구현 반환 (실제 S3 호출 안함)
        return AmazonS3ClientBuilder.standard()
                .withRegion(region)
                .withCredentials(new AWSStaticCredentialsProvider(
                        new BasicAWSCredentials("dummy", "dummy")))
                .build();
    }
}