package com.runnity.global.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {
    /**
     * 지정한 prefix(도메인 폴더) 하위에 업로드하고 최종 접근 URL을 반환.
     * 예: prefix="profile-photos/123" → profile-photos/123/{uuid}.jpg
     */
    String upload(String prefix, MultipartFile file);

    /**
     * 저장소에서 리소스 삭제 (url 또는 key 둘 다 받을 수 있게 처리)
     */
    void delete(String urlOrKey);

    /**
     * 파일 확장자/크기 검증 (원한다면 디폴트 제공)
     */
    default void validateImage(MultipartFile file, long maxBytes) {
        String name = file.getOriginalFilename();
        if (name == null || !name.contains(".")) {
            throw new IllegalArgumentException("파일명이 유효하지 않습니다.");
        }
        String ext = name.substring(name.lastIndexOf(".")).toLowerCase();
        if (!ext.matches("\\.(jpg|jpeg|png|gif|webp)")) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. (jpg, jpeg, png, gif, webp)");
        }
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException("파일 크기는 " + (maxBytes / (1024 * 1024)) + "MB 이하여야 합니다.");
        }
    }
}
