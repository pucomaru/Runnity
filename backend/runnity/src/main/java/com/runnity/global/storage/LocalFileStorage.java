package com.runnity.global.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(name = "SPRING_PROFILES_ACTIVE", havingValue = "local")
@RequiredArgsConstructor
public class LocalFileStorage implements FileStorage {

    @Value("${UPLOAD_PATH:./uploads}")
    private String uploadPath;

    @Override
    public String upload(String prefix, MultipartFile file) {
        validateImage(file, 5L * 1024 * 1024); // 5MB

        try {
            String original = file.getOriginalFilename();
            String ext = original.substring(original.lastIndexOf(".")).toLowerCase();

            Path dir = Paths.get(uploadPath, prefix);
            Files.createDirectories(dir);

            String newName = UUID.randomUUID() + ext;
            Path dest = dir.resolve(newName);
            Files.write(dest, file.getBytes(), StandardOpenOption.CREATE_NEW);

            // 정적 리소스 매핑: /uploads/** → file:${file.upload.path}/
            String url = "/uploads/" + prefix + "/" + newName;
            log.info("LOCAL upload ok: {}", url);
            return url;
        } catch (IOException e) {
            throw new RuntimeException("로컬 파일 저장 실패", e);
        }
    }

    @Override
    public void delete(String urlOrKey) {
        try {
            // url → 실제 파일 경로로 변환
            if (urlOrKey == null || urlOrKey.isBlank()) return;
            String relative = urlOrKey.replaceFirst("^/uploads/", "");
            Path target = Paths.get(uploadPath).resolve(relative);
            Files.deleteIfExists(target);
            log.info("LOCAL delete ok: {}", target);
        } catch (Exception e) {
            log.warn("LOCAL delete fail: {}", e.getMessage());
        }
    }
}
