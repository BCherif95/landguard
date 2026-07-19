package com.laboussole.infrastructure.storage;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PRD 2.1 — documents are sealed at rest with AES-256-GCM: content is
 * unreadable on disk and any tampering breaks decryption.
 */
class AesGcmStorageEncryptionTest {

    private static final byte[] DOCUMENT =
            "Titre Foncier TF-4521/BKO — Moussa Traoré".getBytes(StandardCharsets.UTF_8);

    private final AesGcmStorageEncryption encryption = new AesGcmStorageEncryption(configured());

    @Test
    void roundTripRestoresTheExactDocument() throws IOException {
        byte[] stored = encryption.encrypt(new ByteArrayInputStream(DOCUMENT)).readAllBytes();
        byte[] restored = encryption.decrypt(new ByteArrayInputStream(stored)).readAllBytes();

        assertArrayEquals(DOCUMENT, restored);
    }

    @Test
    void storedBytesNeverContainThePlaintext() throws IOException {
        byte[] stored = encryption.encrypt(new ByteArrayInputStream(DOCUMENT)).readAllBytes();

        assertTrue(stored.length > DOCUMENT.length);
        assertFalse(new String(stored, StandardCharsets.ISO_8859_1).contains("Moussa"));
    }

    @Test
    void eachEncryptionUsesAFreshIv() throws IOException {
        byte[] first = encryption.encrypt(new ByteArrayInputStream(DOCUMENT)).readAllBytes();
        byte[] second = encryption.encrypt(new ByteArrayInputStream(DOCUMENT)).readAllBytes();

        assertFalse(Arrays.equals(first, second));
    }

    @Test
    void tamperingWithTheStoredFileBreaksDecryption() throws IOException {
        byte[] stored = encryption.encrypt(new ByteArrayInputStream(DOCUMENT)).readAllBytes();
        stored[stored.length - 1] ^= 0x01;

        var corrupted = encryption.decrypt(new ByteArrayInputStream(stored));
        assertThrows(IOException.class, corrupted::readAllBytes);
    }

    @Test
    void legacyPlaintextFilesAreServedUnchanged() throws IOException {
        byte[] restored = encryption.decrypt(new ByteArrayInputStream(DOCUMENT)).readAllBytes();

        assertArrayEquals(DOCUMENT, restored);
    }

    @Test
    void blankKeyDisablesEncryptionButStillServesPlaintext() throws IOException {
        var disabled = new AesGcmStorageEncryption(new StorageEncryptionProperties(""));

        assertFalse(disabled.enabled());
        byte[] stored = disabled.encrypt(new ByteArrayInputStream(DOCUMENT)).readAllBytes();
        assertArrayEquals(DOCUMENT, stored);
        assertArrayEquals(DOCUMENT, disabled.decrypt(new ByteArrayInputStream(DOCUMENT)).readAllBytes());
    }

    @Test
    void encryptedFileWithoutAKeyFailsLoudly() throws IOException {
        byte[] stored = encryption.encrypt(new ByteArrayInputStream(DOCUMENT)).readAllBytes();
        var disabled = new AesGcmStorageEncryption(new StorageEncryptionProperties(""));

        assertThrows(IllegalStateException.class,
                () -> disabled.decrypt(new ByteArrayInputStream(stored)));
    }

    @Test
    void rejectsKeysThatAreNotAes256() {
        var shortKey = Base64.getEncoder().encodeToString(new byte[16]);
        var failure = assertThrows(IllegalStateException.class,
                () -> new AesGcmStorageEncryption(new StorageEncryptionProperties(shortKey)));
        assertTrue(failure.getMessage().contains("32 bytes"));
    }

    private static StorageEncryptionProperties configured() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        return new StorageEncryptionProperties(Base64.getEncoder().encodeToString(key));
    }
}
