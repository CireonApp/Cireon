package com.cireonapp.server.domain.user;

import com.cireonapp.server.domain.session.SessionManager;
import com.cireonapp.server.initializer.Databases;
import com.cireonapp.server.util.EncryptionHelper;
import com.cireonapp.server.util.WebThemes;
import jakarta.annotation.Nullable;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.exceptions.UniqueConstraintException;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.repository.Cursor;

import java.util.Optional;

import static org.dizitart.no2.filters.FluentFilter.where;

public class UserManager {
    /**
     * Creates a new user. If there are no users in the database, the first user will be set to administrator.
     *
     * @param user the user to create
     * @return true if the user was created successfully, false otherwise.
     */
    public static boolean create(User user) throws UniqueConstraintException {
        if (user == null || user.getUsername() == null || user.getUsername().isBlank()) {
            return false;
        }
        if (!atLeastOneAdmin()) {
            //first user must be set to administrator
            user.setAdministrator(true);
        }
        user.setPassword(EncryptionHelper.encryptPassword_argon2(user.getPassword()));
        WriteResult result = Databases.getUserRepository().insert(user);
        return result.getAffectedCount() > 0;
    }

    private static boolean atLeastOneAdmin() {
        return !Databases.getUserRepository().find(where("administrator").eq(true)).isEmpty();
    }

    public static boolean updateLastUse(User user) {
        return updateLastUse(user.getUsername());
    }

    public static boolean updateLastUse(String username) {
        Optional<User> optUser = get(username);
        if (optUser.isEmpty()) return false;

        User user = optUser.get();
        user.updateLastUseDate();

        Databases.getUserRepository().update(user);
        return true;
    }

    public static boolean update(String username, User updateInfo) {
        if (!exists(username)) return false;

        Optional<User> existingUserOpt = get(username);
        if (existingUserOpt.isEmpty()) return false;

        User existingUser = existingUserOpt.get();

        // Dynamically merge all non-null fields from updateInfo
        // will also merge non-null user settings.
        existingUser.merge(updateInfo);

        // Update in database
        Databases.getUserRepository().update(existingUser);
        return true;
    }

    /**
     * Deletes a user from the database.
     *
     * @param user The user to delete.
     * @return Returns true if the user was deleted successfully, false otherwise.
     */
    public static boolean delete(User user) {
        WriteResult result = Databases.getUserRepository().remove(user);
        SessionManager.deleteAllForUser(user.getUsername(), null, true);
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
        return Optional.ofNullable(Databases.getUserRepository().getById(username));
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
    public static Cursor<User> getAll() {
        return Databases.getUserRepository().find(Filter.ALL);
    }

    public static int getCount() {
        return Math.toIntExact(Databases.getUserRepository().size());
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
