package com.runnity.global.storage;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(name = "SPRING_PROFILES_ACTIVE", havingValue = "prod")
@RequiredArgsConstructor
public class AwsS3FileStorage implements FileStorage {

    private final AmazonS3 amazonS3;

    @Value("${AWS_BUCKET:AWS_BUCKET}")
    private String bucket;

    @Value("${AWS_STATIC:AWS_STATIC}")
    private String region;

    @Override
    public String upload(String prefix, MultipartFile file) {
        String key = prefix + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());

            amazonS3.putObject(new PutObjectRequest(bucket, key, file.getInputStream(), metadata));

            return key;  // ← S3 키만 반환 (URL 아님!)
        } catch (IOException e) {
            throw new RuntimeException("S3 upload failed", e);
        }
    }

    @Override
    public void delete(String urlOrKey) {
        try {
            if (urlOrKey == null || urlOrKey.isBlank()) return;

            // url 또는 key 모두 허용: url을 받으면 key 추출
            String key = urlOrKey;
            if (urlOrKey.startsWith("http")) {
                // https://{bucket}.s3.{region}.amazonaws.com/{key}
                URI uri = URI.create(urlOrKey);
                String path = uri.getPath(); // /{key}
                key = path.startsWith("/") ? path.substring(1) : path;
            }
            amazonS3.deleteObject(bucket, key);
            log.info("S3 delete ok: {}", key);
        } catch (Exception e) {
            log.warn("S3 delete fail: {}", e.getMessage());
        }
    }
}
