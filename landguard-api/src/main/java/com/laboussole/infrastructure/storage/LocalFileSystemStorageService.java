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
    private final AesGcmStorageEncryption encryption;

    public LocalFileSystemStorageService(
            @Value("${laboussole.storage.root}") String root,
            AesGcmStorageEncryption encryption) {
        this.encryption = encryption;
        // Resolved to an absolute, normalised path once, here. Every containment
        // check below compares against it, and comparing a normalised path with
        // an unnormalised one never matches: a relative root such as `./uploads`
        // would make every store fail with a bogus traversal error.
        this.rootLocation = Paths.get(root).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Could not initialize storage location " + rootLocation, e);
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

            String uniqueName = UUID.randomUUID() + extension;
            Path destinationFile = resolveInsideRoot(uniqueName);

            Files.copy(encryption.encrypt(content), destinationFile, StandardCopyOption.REPLACE_EXISTING);
            return uniqueName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file.", e);
        }
    }

    @Override
    public InputStream load(String storageKey) {
        try {
            return encryption.decrypt(Files.newInputStream(resolveInsideRoot(storageKey)));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load file: " + storageKey, e);
        }
    }

    @Override
    public void delete(String storageKey) {
        try {
            // Guarded like the others: an unchecked resolve here would let a
            // crafted key delete land titles anywhere on the filesystem.
            Files.deleteIfExists(resolveInsideRoot(storageKey));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file.", e);
        }
    }

    /**
     * Resolves a storage key against the root and refuses anything that escapes
     * it. Storage is deliberately flat: a key is a file name, never a path, so
     * the resolved parent must be the root itself.
     */
    private Path resolveInsideRoot(String storageKey) {
        Path resolved = rootLocation.resolve(storageKey).normalize().toAbsolutePath();
        if (!rootLocation.equals(resolved.getParent())) {
            throw new IllegalArgumentException(
                    "Clé de stockage invalide : le fichier sortirait du répertoire de stockage.");
        }
        return resolved;
    }
}
