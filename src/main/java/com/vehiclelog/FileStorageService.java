package com.vehiclelog;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {
    private final Path storageDirectory;

    public FileStorageService(@Value("${app.storage.directory}") String directory) throws IOException {
        storageDirectory = Path.of(directory).toAbsolutePath().normalize();
        Files.createDirectories(storageDirectory);
    }

    public String store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Choose a document before uploading.");
        }
        String originalName = Path.of(file.getOriginalFilename() == null ? "document" : file.getOriginalFilename())
                .getFileName().toString();
        Path destination = storageDirectory.resolve(UUID.randomUUID() + "-" + originalName).normalize();
        if (!destination.startsWith(storageDirectory)) {
            throw new IllegalArgumentException("Invalid document name.");
        }
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return destination.toString();
    }
}
