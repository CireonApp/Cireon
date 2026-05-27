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
    private static SecureRandom secureRandom = new SecureRandom();

    private static String generateToken() {
        byte[] randomBytes = new byte[16];
        secureRandom.nextBytes(randomBytes);

        return HexFormat.of().formatHex(randomBytes);
    }


    public static boolean isValid(String token) {
        Optional<Session> sessionOpt = get(token);

        if (sessionOpt.isEmpty())
            return false;


        if (!UserManager.exists(sessionOpt.get().getUsername()))
            return false;

        return true;
    }

    public static Optional<Session> create(String username, String device) {
        if (!UserManager.exists(username)) return Optional.empty();
        String token = generateToken();
        Session newSession = new Session(EncryptionHelper.hashSHA256(token), username, TimeHelper.getCurrentTimeISO(), device);
        WriteResult result = Databases.sessionRepository.insert(newSession);
        if (result.getAffectedCount() > 0) {
            newSession.setToken(token);// return the actual token and not encrypted one. send to the user later via the api
            return Optional.of(newSession);
        } else {
            return Optional.empty();
        }
    }

    public static boolean delete(String token) {
        token = EncryptionHelper.hashSHA256(token);
        Session session = Databases.sessionRepository.getById(token);
        if (session == null) return false;
        WriteResult result = Databases.sessionRepository.remove(session);
        return result.getAffectedCount() > 0;
    }

    public static Optional<Session> get(String token) {
        token = EncryptionHelper.hashSHA256(token);
        return Optional.ofNullable(Databases.sessionRepository.getById(token));
    }

    public static boolean deleteAllForUser(String username, String token, boolean deleteCurrent) {
        token = EncryptionHelper.hashSHA256(token);
        Filter filter = where("username").eq(username);
        if (!deleteCurrent) {
            filter = Filter.and(filter, where("token").notEq(token));
        }
        WriteResult result = Databases.sessionRepository.remove(filter);
        return result.getAffectedCount() > 0;
    }

    public static Cursor<Session> getAllForUser(String username) {
        return Databases.sessionRepository.find(where("username").eq(username));
    }
}
