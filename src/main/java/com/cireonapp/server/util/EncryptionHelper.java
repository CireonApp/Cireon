package com.cireonapp.server.util;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

public class EncryptionHelper {
    private static final Argon2PasswordEncoder arg2SpringSecurity = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();


    public static String encryptPassword_argon2(String password){
        return arg2SpringSecurity.encode(password);

    }

    public static boolean matchesPassword_argon2(String rawPassword, String encodedPassword) {
        return arg2SpringSecurity.matches(rawPassword, encodedPassword);
    }
}
