package com.cireonapp.server.domain.media.movie;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;

public class MovieConverter implements EntityConverter<Movie> {
    @Override
    public Class<Movie> getEntityType() {
        return Movie.class;
    }

    @Override
    public Document toDocument(Movie entity, NitriteMapper nitriteMapper) {
        return null;
    }

    @Override
    public Movie fromDocument(Document document, NitriteMapper nitriteMapper) {
        return null;
    }
}
