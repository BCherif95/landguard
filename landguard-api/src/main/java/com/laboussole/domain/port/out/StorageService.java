package com.laboussole.domain.port.out;

import java.io.InputStream;

/**
 * Port for persisting binary objects (PDFs, images) to a storage backend.
 */
public interface StorageService {

    /**
     * Stores a file and returns its unique storage key/path.
     */
    String store(InputStream content, String fileName, String contentType);

    /**
     * Opens a stored file for reading. The caller must close the stream.
     */
    InputStream load(String storageKey);

    /**
     * Deletes a file by its storage key.
     */
    void delete(String storageKey);
}
