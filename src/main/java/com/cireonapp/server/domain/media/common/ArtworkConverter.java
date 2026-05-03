package com.cireonapp.server.domain.media.common;

import com.cireonapp.server.domain.session.Session;
import com.cireonapp.server.util.CustomConverterReadsHelper;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;

import java.nio.file.Path;

public class ArtworkConverter implements EntityConverter<Artwork> {
    @Override
    public Class<Artwork> getEntityType() {
        return Artwork.class;
    }

    @Override
    public Document toDocument(Artwork artwork, NitriteMapper nitriteMapper) {
//        return Document.createDocument()
//                .put("logo", artwork.getLogo().toString())
//                .put("background", artwork.getBackground().toString())
//                .put("poster", artwork.getPoster().toString());
        return null;
    }

    @Override
    public Artwork fromDocument(Document document, NitriteMapper nitriteMapper) {
//        Artwork session = new Artwork();
//        session.setLogo(document, "logo",String.class);
//        session.setBackground(CustomConverterReadsHelper.readPath(document, "background"));
//        session.setPoster(CustomConverterReadsHelper.readPath(document, "poster"));
//        return session;
        return null;
    }


}
