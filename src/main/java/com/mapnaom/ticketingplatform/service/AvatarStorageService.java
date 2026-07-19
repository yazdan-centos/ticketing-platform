package com.mapnaom.ticketingplatform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

@Service
public class AvatarStorageService {

    @Value("${file.upload-dir:${user.home}/ticketing-platform/uploads}")
    private String uploadDir;

    public String storeAvatar(String scope, Long entityId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Avatar must be an image file");
        }

        try {
            String extension = resolveExtension(file);
            Path relativePath = Paths.get("avatars", scope, String.valueOf(entityId), "avatar." + extension);
            Path absolutePath = Paths.get(uploadDir).resolve(relativePath);
            Files.createDirectories(absolutePath.getParent());
            Files.copy(file.getInputStream(), absolutePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + relativePath.toString().replace('\\', '/');
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store avatar", ex);
        }
    }

    public void deleteAvatar(String scope, Long entityId, String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) {
            return;
        }

        Path avatarDirectory = Paths.get(uploadDir)
                .resolve(Paths.get("avatars", scope, String.valueOf(entityId)))
                .normalize();
        Path uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Path absoluteDirectory = avatarDirectory.toAbsolutePath().normalize();

        if (!absoluteDirectory.startsWith(uploadRoot)) {
            throw new IllegalArgumentException("Invalid avatar path");
        }

        try (var files = Files.list(absoluteDirectory)) {
            files.filter(Files::isRegularFile).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ex) {
                    throw new RuntimeException("Failed to delete avatar", ex);
                }
            });
            Files.deleteIfExists(absoluteDirectory);
        } catch (java.nio.file.NoSuchFileException ignored) {
            // The database reference can still be cleared if the file is already gone.
        } catch (IOException ex) {
            throw new RuntimeException("Failed to delete avatar", ex);
        }
    }

    private String resolveExtension(MultipartFile file) {
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (extension != null && !extension.isBlank()) {
            return extension.toLowerCase(Locale.ROOT);
        }

        String contentType = file.getContentType();
        if ("image/png".equalsIgnoreCase(contentType)) return "png";
        if ("image/jpeg".equalsIgnoreCase(contentType)) return "jpg";
        if ("image/jpg".equalsIgnoreCase(contentType)) return "jpg";
        if ("image/gif".equalsIgnoreCase(contentType)) return "gif";
        if ("image/webp".equalsIgnoreCase(contentType)) return "webp";
        return "png";
    }
}
