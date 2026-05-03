package com.cireonapp.server.domain.session;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;

public class SessionConverter implements EntityConverter<Session> {

    @Override
    public Class<Session> getEntityType() {
        return Session.class;
    }

    @Override
    public Document toDocument(Session session, NitriteMapper nitriteMapper) {
        return Document.createDocument()
                .put("token", session.getToken())
                .put("username", session.getUsername())
                .put("creationTime", session.getCreationTime())
                .put("device", session.getDevice());
    }

    @Override
    public Session fromDocument(Document document, NitriteMapper nitriteMapper) {
        Session session = new Session();
        session.setUsername(document.get("username", String.class));
        session.setDevice(document.get("device", String.class));
        session.setCreationTime(document.get("creationTime", String.class));
        session.setToken(document.get("token", String.class));
        return session;
    }
}
