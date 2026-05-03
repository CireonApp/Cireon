package com.cireonapp.server.util;

import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.security.MessageDigest;

public class ContentHashHelper {
    private static final long CHUNK = 4L * 1024 * 1024;
    private static final long FULL_HASH_LIMIT = 12L * 1024 * 1024;

    public static String hashFile(Path path) {
        try {
            long size = java.nio.file.Files.size(path);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r")) {

                if (size <= FULL_HASH_LIMIT) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = raf.read(buffer)) != -1) {
                        digest.update(buffer, 0, read);
                    }
                } else {
                    byte[] buffer = new byte[(int) CHUNK];

                    raf.seek(0);
                    int readStart = raf.read(buffer);
                    if (readStart > 0) {
                        digest.update(buffer, 0, readStart);
                    }

                    long lastPos = Math.max(0, size - CHUNK);
                    raf.seek(lastPos);
                    int readEnd = raf.read(buffer);
                    if (readEnd > 0) {
                        digest.update(buffer, 0, readEnd);
                    }

                    digest.update(longToBytes(size));
                }
            }

            return bytesToHex(digest.digest());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] longToBytes(long x) {
        byte[] b = new byte[8];
        for (int i = 7; i >= 0; i--) {
            b[i] = (byte) (x & 0xff);
            x >>= 8;
        }
        return b;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
