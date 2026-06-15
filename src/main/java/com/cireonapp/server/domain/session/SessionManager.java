package com.cireonapp.server.domain.session;

import com.cireonapp.server.domain.user.UserManager;
import com.cireonapp.server.initializer.Databases;
import com.cireonapp.server.util.EncryptionHelper;
import com.cireonapp.server.util.TimeHelper;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.filters.Filter;
import org.dizitart.no2.repository.Cursor;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Optional;

import static org.dizitart.no2.filters.FluentFilter.where;

public class SessionManager {
    public static final long SESSION_EXPIRATION_TIME_SECONDS = 2592000L; // 30 days in seconds
    private static final SecureRandom secureRandom = new SecureRandom();

    private static Optional<Session> findByRawToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        String hashedToken = EncryptionHelper.hashSHA256(token);
        return Optional.ofNullable(Databases.getSessionRepository().getById(hashedToken));
    }

    private static boolean isSessionUsable(Session session) {
        if (session == null) {
            return false;
        }

        String username = session.getUsername();
        if (username == null || username.isBlank()) {
            return false;
        }

        if (!UserManager.exists(username)) {
            return false;
        }

        return !sessionExpired(session.getCreationTime());
    }

    private static String generateToken() {
        byte[] randomBytes = new byte[16];
        secureRandom.nextBytes(randomBytes);

        return HexFormat.of().formatHex(randomBytes);
    }


    public static boolean isValid(String token) {
        Optional<Session> sessionOpt = findByRawToken(token);
        return sessionOpt.filter(SessionManager::isSessionUsable).isPresent();
    }


    public static boolean sessionExpired(String timeISO) {
        long sessionTime = TimeHelper.parseTimeFromISO(timeISO);
        if (sessionTime < 0) {
            return true;
        }

        long currentTime = TimeHelper.getCurrentTime();

        return currentTime - sessionTime > SESSION_EXPIRATION_TIME_SECONDS;
    }

    public static Optional<Session> create(String username, String device) {
        if (!UserManager.exists(username)) return Optional.empty();
        String token = generateToken();
        Session newSession = new Session(EncryptionHelper.hashSHA256(token), username, TimeHelper.getCurrentTimeISO(), device);
        WriteResult result = Databases.getSessionRepository().insert(newSession);
        if (result.getAffectedCount() > 0) {
            newSession.setToken(token);// return the actual token and not encrypted one. send to the user later via the api
            return Optional.of(newSession);
        } else {
            return Optional.empty();
        }
    }

    public static boolean delete(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        token = EncryptionHelper.hashSHA256(token);
        Session session = Databases.getSessionRepository().getById(token);
        if (session == null) return false;
        WriteResult result = Databases.getSessionRepository().remove(session);
        return result.getAffectedCount() > 0;
    }

    public static Optional<Session> get(String token) {
        Optional<Session> sessionOpt = findByRawToken(token);
        if (sessionOpt.isEmpty()) {
            return Optional.empty();
        }

        Session session = sessionOpt.get();
        if (sessionExpired(session.getCreationTime())) {
            Databases.getSessionRepository().remove(session);
            return Optional.empty();
        }

        String username = session.getUsername();
        if (username == null || username.isBlank() || !UserManager.exists(username)) {
            return Optional.empty();
        }

        return Optional.of(session);
    }

    public static void deleteAllForUser(String username, String token, boolean deleteCurrent) {
        Filter filter = where("username").eq(username);
        if (!deleteCurrent) {
            if (token == null || token.isBlank()) {
                return;
            }
            String hashedToken = EncryptionHelper.hashSHA256(token);
            filter = Filter.and(filter, where("token").notEq(hashedToken));
        }
        WriteResult result = Databases.getSessionRepository().remove(filter);
        result.getAffectedCount();
    }

    public static Cursor<Session> getAllForUser(String username) {
        return Databases.getSessionRepository().find(where("username").eq(username));
    }
}
