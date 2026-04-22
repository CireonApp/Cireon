package com.cireonapp.server.domain.user;

import com.cireonapp.server.initializer.Databases;
import org.dizitart.no2.common.WriteResult;

import java.util.Optional;
import java.util.Set;

public class UserManager {
    public static boolean createUser(User user) {
        if(getAllUsers().isEmpty()){
            user.setPermissions(Set.of(UserPermissions.ADMINISTRATOR));
        }
        WriteResult result = Databases.userRepository.insert(user);
        return result.getAffectedCount() > 0;
    }

    public static boolean deleteUser(User user) {
        WriteResult result = Databases.userRepository.remove(user);
        return result.getAffectedCount() > 0;
    }

    public static Optional<User> getUser(String username) {
        return Optional.ofNullable(Databases.userRepository.getById(username));
    }

    public static Set<User> getAllUsers() {
        return Databases.userRepository.find().toSet();
    }
}
