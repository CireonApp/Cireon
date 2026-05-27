package com.cireonapp.server.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.codec.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptionHelper {
    private static final Argon2PasswordEncoder arg2SpringSecurity = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    public static String encryptPassword_argon2(String password) {
        return arg2SpringSecurity.encode(password);
    }

    public static boolean matchesPassword_argon2(String rawPassword, String encodedPassword) {
        return arg2SpringSecurity.matches(rawPassword, encodedPassword);
    }


    public static String hashSHA256(String text) {
//        MessageDigest digest = null;
//        try{
//             digest = MessageDigest.getInstance("SHA-256");
//        }catch (NoSuchAlgorithmException ignored){
//
//        }
//        assert digest != null;
//        byte[] hashBytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
//
//        return new String(Hex.encode(hashBytes));
        return DigestUtils.sha256Hex(text);
    }


    public static boolean validateHashSha256(String incomingToken, String storedHash) {
        if (incomingToken == null || storedHash == null) {
            return false;
        }
        String incomingHash = hashSHA256(incomingToken);
        return MessageDigest.isEqual(
                incomingHash.getBytes(StandardCharsets.UTF_8),
                storedHash.getBytes(StandardCharsets.UTF_8)
        );
    }
}
