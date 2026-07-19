package com.laboussole.infrastructure.storage;

import com.laboussole.domain.port.out.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalFileSystemStorageService implements StorageService {

    private final Path rootLocation;

    public LocalFileSystemStorageService(@Value("${laboussole.storage.root:/Users/cherif/Projects/uploads/landguard}") String root) {
        this.rootLocation = Paths.get(root);
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    @Override
    public String store(InputStream content, String fileName, String contentType) {
        try {
            if (content == null) {
                throw new IllegalArgumentException("Failed to store empty file.");
            }
            
            String extension = "";
            int i = fileName.lastIndexOf('.');
            if (i > 0) {
                extension = fileName.substring(i);
            }
            
            String uniqueName = UUID.randomUUID().toString() + extension;
            Path destinationFile = this.rootLocation.resolve(uniqueName).normalize().toAbsolutePath();
            
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                // Security check
                throw new RuntimeException("Cannot store file outside current directory.");
            }
            
            Files.copy(content, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            return uniqueName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    @Override
    public InputStream load(String storageKey) {
        try {
            Path file = rootLocation.resolve(storageKey).normalize().toAbsolutePath();
            if (!file.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new IllegalArgumentException("Cannot read file outside storage directory.");
            }
            return Files.newInputStream(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file: " + storageKey, e);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            Path file = rootLocation.resolve(storageKey);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file.", e);
        }
    }
}
