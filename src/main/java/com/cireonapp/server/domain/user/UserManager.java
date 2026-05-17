package com.cireonapp.server.domain.user;

import com.cireonapp.server.initializer.Databases;
import com.cireonapp.server.util.EncryptionHelper;
import com.cireonapp.server.util.WebThemes;
import jakarta.annotation.Nullable;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.exceptions.UniqueConstraintException;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public class UserManager {
    /**
     * Creates a new user. If there are no users in the database, the first user will be set to administrator.
     *
     * @param user
     * @return true if the user was created successfully, false otherwise.
     */
    public static boolean create(User user) {
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

    public static boolean update(String username, User updateInfo) {
        if (!exists(username)) return false;

        Optional<User> existingUserOpt = get(username);
        if (existingUserOpt.isEmpty()) return false;

        User existingUser = existingUserOpt.get();

        // Dynamically merge all non-null/non-empty fields from updateInfo
        mergeFields(existingUser, updateInfo);

        // Update in database
        Databases.userRepository.update(existingUser);
        return true;
    }

    /**
     * Merges non-null/non-empty fields from source to target using reflection.
     * Skips the 'username' field to prevent accidental changes.
     * Special handling for 'password' field (applies encryption).
     */
    private static void mergeFields(User target, User source) {
        Field[] fields = User.class.getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);

            try {
                // Skip username field - never allow changes
                if ("username".equals(field.getName())) {
                    continue;
                }

                Object sourceValue = field.get(source);

                // Skip null values
                if (sourceValue == null) {
                    continue;
                }

                // Skip empty strings
                if (sourceValue instanceof String && ((String) sourceValue).isBlank()) {
                    continue;
                }

                // Skip empty collections
                if (sourceValue instanceof Collection && ((Collection<?>) sourceValue).isEmpty()) {
                    continue;
                }

                // Special handling for password: encrypt before setting
                if ("password".equals(field.getName())) {
                    field.set(target, EncryptionHelper.encryptPassword_argon2((String) sourceValue));
                } else {
                    // Copy any other non-null/non-empty field
                    field.set(target, sourceValue);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to merge field: " + field.getName(), e);
            }
        }
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

    public static int getCount() {
        return Math.toIntExact(Databases.userRepository.size());
    }

    /**
     * Get a theme from a USER
     *
     * @param user The user to get the theme label for
     * @return The theme label for the user, or the default theme if the user is null
     */
    public static WebThemes getThemeLabel(@Nullable User user) {
        if (user == null) return WebThemes.DARK;

        return user.getSettings().getWebTheme();
    }
}
