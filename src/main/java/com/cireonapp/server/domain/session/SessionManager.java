package com.cireonapp.server.domain.session;

import com.cireonapp.server.domain.user.UserManager;
import com.cireonapp.server.initializer.Databases;
import com.cireonapp.server.util.TimeHelper;
import org.dizitart.no2.common.WriteResult;
import org.dizitart.no2.filters.Filter;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.dizitart.no2.filters.FluentFilter.where;

public class SessionManager {
    private static String generateToken() {
        return UUID.randomUUID().toString();
    }

    public static boolean isValid(String token) {
        return Databases.sessionRepository.getById(token) != null;
    }

    public static Optional<Session> create(String username, String device) {
        if (!UserManager.exists(username)) return Optional.empty();
        Session newSession = new Session(generateToken(), username, TimeHelper.getCurrentTimeISO(), device);
        WriteResult result = Databases.sessionRepository.insert(newSession);
        if (result.getAffectedCount() > 0) {
            return Optional.of(newSession);
        } else {
            return Optional.empty();
        }
    }

    public static boolean delete(String token) {
        Session session = Databases.sessionRepository.getById(token);
        if (session == null) return false;
        WriteResult result = Databases.sessionRepository.remove(session);
        return result.getAffectedCount() > 0;
    }

    public static Optional<Session> get(String token) {
        return Optional.ofNullable(Databases.sessionRepository.getById(token));
    }
    
    public static boolean deleteAllForUser(String username,String token, boolean deleteCurrent) {
        Filter filter = where("username").eq(username);
        if(!deleteCurrent){
            filter = Filter.and(filter, where("token").notEq(token));
        }
        WriteResult result = Databases.sessionRepository.remove(filter);
        return result.getAffectedCount() > 0;
    }

    public static Set<Session> getAllForUser(String username) {
        return Databases.sessionRepository.find(where("username").eq(username)).toSet();
    }
}
