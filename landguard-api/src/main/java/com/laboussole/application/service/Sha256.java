package com.laboussole.application.service;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/** Hex-encoded SHA-256 fingerprints for document sealing (PRD 2.1). */
public final class Sha256 {

    private Sha256() {
    }

    public static String hex(byte[] content) {
        return HexFormat.of().formatHex(newDigest().digest(content));
    }

    public static String hex(InputStream content) throws IOException {
        var digest = newDigest();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = content.read(buffer)) != -1) {
            digest.update(buffer, 0, read);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
