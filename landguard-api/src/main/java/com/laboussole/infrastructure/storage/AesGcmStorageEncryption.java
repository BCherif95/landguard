package com.laboussole.infrastructure.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.SequenceInputStream;
import java.io.UncheckedIOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM envelope for stored documents (PRD 2.1).
 *
 * <p>Encrypted files start with the magic bytes {@code LGE1}, followed by a
 * random 96-bit IV and the GCM ciphertext (authentication tag included). Any
 * bit flipped in a stored file makes decryption fail — the read stream throws
 * before serving corrupted content.
 *
 * <p>Files without the magic header (uploaded before encryption was enabled)
 * are served unchanged, so enabling the key never breaks existing storage.
 */
@Component
public class AesGcmStorageEncryption {

    static final byte[] MAGIC = {'L', 'G', 'E', '1'};
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final int KEY_LENGTH_BYTES = 32;

    private static final Logger log = LoggerFactory.getLogger(AesGcmStorageEncryption.class);

    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public AesGcmStorageEncryption(StorageEncryptionProperties properties) {
        if (!properties.isConfigured()) {
            this.key = null;
            log.warn("Document storage encryption is DISABLED (blank laboussole.storage.encryption.key). "
                    + "PRD 2.1 requires AES-256 at rest outside local development.");
            return;
        }
        byte[] rawKey;
        try {
            rawKey = Base64.getDecoder().decode(properties.key().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "laboussole.storage.encryption.key is not valid base64", e);
        }
        if (rawKey.length != KEY_LENGTH_BYTES) {
            throw new IllegalStateException(
                    "laboussole.storage.encryption.key must decode to 32 bytes (AES-256), got "
                            + rawKey.length);
        }
        this.key = new SecretKeySpec(rawKey, "AES");
    }

    public boolean enabled() {
        return key != null;
    }

    /** Wraps a plaintext stream into the {@code LGE1 || IV || ciphertext} envelope. */
    public InputStream encrypt(InputStream plaintext) {
        if (key == null) {
            return plaintext;
        }
        byte[] iv = new byte[IV_LENGTH_BYTES];
        random.nextBytes(iv);
        var header = new byte[MAGIC.length + IV_LENGTH_BYTES];
        System.arraycopy(MAGIC, 0, header, 0, MAGIC.length);
        System.arraycopy(iv, 0, header, MAGIC.length, IV_LENGTH_BYTES);
        return new SequenceInputStream(
                new ByteArrayInputStream(header),
                new CipherInputStream(plaintext, newCipher(Cipher.ENCRYPT_MODE, iv)));
    }

    /**
     * Unwraps a stored stream: enveloped files are decrypted (and
     * authenticated), legacy plaintext files are returned as-is.
     */
    public InputStream decrypt(InputStream stored) {
        try {
            var pushback = new PushbackInputStream(stored, MAGIC.length);
            byte[] head = pushback.readNBytes(MAGIC.length);
            if (!isMagic(head)) {
                pushback.unread(head);
                return pushback;
            }
            if (key == null) {
                throw new IllegalStateException(
                        "Stored file is encrypted but laboussole.storage.encryption.key is blank");
            }
            byte[] iv = pushback.readNBytes(IV_LENGTH_BYTES);
            if (iv.length != IV_LENGTH_BYTES) {
                throw new IOException("Truncated encryption header");
            }
            return new CipherInputStream(pushback, newCipher(Cipher.DECRYPT_MODE, iv));
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read stored file header", e);
        }
    }

    private static boolean isMagic(byte[] head) {
        if (head.length != MAGIC.length) {
            return false;
        }
        for (int i = 0; i < MAGIC.length; i++) {
            if (head[i] != MAGIC[i]) {
                return false;
            }
        }
        return true;
    }

    private Cipher newCipher(int mode, byte[] iv) {
        try {
            var cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(mode, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return cipher;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("AES-256-GCM unavailable", e);
        }
    }
}
