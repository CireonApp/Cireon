package com.cireonapp.server.domain.user;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;

import java.util.HashSet;
import java.util.Set;

public class UserConverter implements EntityConverter<User> {
    @Override
    public Class<User> getEntityType() {
        return User.class;
    }

    @Override
    public Document toDocument(User user, NitriteMapper nitriteMapper) {
        return Document.createDocument()
                .put("username", user.getUsername())
                .put("password", user.getPassword())
                .put("displayName", user.getDisplayName())
                .put("permissions", user.getPermissions());
    }

    @Override
    public User fromDocument(Document document, NitriteMapper nitriteMapper) {
        User user = new User();
        user.setUsername(document.get("username", String.class));
        user.setPassword(document.get("password", String.class));
        user.setDisplayName(document.get("displayName", String.class));

        Object rawPermissions = document.get("permissions");
        Set<UserPermissions> permissions = new HashSet<>();
        if (rawPermissions instanceof Iterable<?> values) {
            for (Object value : values) {
                if (value instanceof UserPermissions permission) {
                    permissions.add(permission);
                } else if (value != null) {
                    permissions.add(UserPermissions.valueOf(String.valueOf(value)));
                }
            }
        }
        if(permissions.isEmpty()) {
            permissions.add(UserPermissions.USER_READ);
            permissions.add(UserPermissions.CONTENT_READ);
        }
        user.setPermissions(permissions);
        return user;
    }
}
