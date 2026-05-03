package com.cireonapp.server.domain.user;

import com.cireonapp.server.initializer.Databases;
import com.cireonapp.server.util.EncryptionHelper;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.exceptions.UniqueConstraintException;

import java.util.Optional;
import java.util.Set;

public class UserManager {
    /**
     * Creates a new user. If there are no users in the database, the first user will be set to administrator.
     *
     * @param user
     * @return true if the user was created successfully, false otherwise.
     */
    public static boolean create(User user)  {
        if (user == null || user.getUsername() == null || user.getUsername().isBlank()) {
            return false;
        }

        if (getAll().isEmpty()) {
            //first user must be set to administrator
            user.setPermissions(Set.of(UserPermissions.ADMINISTRATOR));
        }
        user.setPassword(EncryptionHelper.encryptPassword_argon2(user.getPassword()));
        WriteResult result = Databases.userRepository.insert(user);
        return result.getAffectedCount() > 0;
    }

    /**
     * Deletes a user from the database.
     *
     * @param user The user to delete.
     * @return Returns true if the user was deleted successfully, false otherwise.
     */
    public static boolean delete(User user) {
        WriteResult result = Databases.userRepository.remove(user);
        return result.getAffectedCount() > 0;
    }

    /**
     * Deletes a user from the database.
     *
     * @param username The username of the user to delete.
     * @return Returns true if the user was deleted successfully, false otherwise.
     */
    public static boolean delete(String username) {
        Optional<User> user = get(username);
        if (user.isEmpty()) return false;

        return delete(user.get());
    }

    public static Optional<User> get(String username) {
        if (username == null || username.isBlank()) return Optional.empty();
        return Optional.ofNullable(Databases.userRepository.getById(username));
    }

    /**
     * Checks if a user with the given username exists in the database.
     *
     * @param username The username to check for existence.
     * @return true if the user exists, false otherwise.
     */
    public static boolean exists(String username) {
        return get(username).isPresent();
    }

    /**
     * @return Returns a set of all users in the database.
     * @warning: This method should be used with caution, as it can potentially return a large number of users if the database is large.
     */
    public static Set<User> getAll() {
        return Databases.userRepository.find().toSet();
    }
}
