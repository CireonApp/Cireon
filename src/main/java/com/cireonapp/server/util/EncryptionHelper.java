package com.cireonapp.server.util;

import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

public class EncryptionHelper {
    private static final Argon2PasswordEncoder arg2SpringSecurity = new Argon2PasswordEncoder(16, 32, 1, 60000, 10);


    public static String encryptPassword_argon2(String password){
        return arg2SpringSecurity.encode(password);

    }

    public static boolean matchesPassword_argon2(String rawPassword, String encodedPassword) {
        return arg2SpringSecurity.matches(rawPassword, encodedPassword);
    }
}
