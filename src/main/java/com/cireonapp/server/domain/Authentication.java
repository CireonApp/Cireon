package com.cireonapp.server.domain;

import com.cireonapp.server.domain.user.User;
import com.cireonapp.server.domain.user.UserManager;
import com.cireonapp.server.util.EncryptionHelper;

import java.util.Optional;

public class Authentication {
    /**
     * Authenticates a user with the given username and password.
     * @param username The username of the user to authenticate.
     * @param password The password of the user to authenticate.
     * @return true if the user was authenticated successfully, false otherwise.
     */
    public static boolean authenticateUser(String username, String password) {
        Optional<User> user = UserManager.get(username);

        if (user.isEmpty()) return false;

        return EncryptionHelper.matchesPassword_argon2(password, user.get().getPassword());
    }
}
