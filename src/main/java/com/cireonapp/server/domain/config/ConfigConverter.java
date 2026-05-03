package com.cireonapp.server.domain.config;

import com.cireonapp.server.util.CustomConverterReadsHelper;
import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;

public class ConfigConverter implements EntityConverter<Config> {
    @Override
    public Class<Config> getEntityType() {
        return Config.class;
    }

    @Override
    public Document toDocument(Config config, NitriteMapper nitriteMapper) {
        return Document.createDocument()
                .put("maxUsers", config.getMaxUsers())
                .put("port", config.getPort());

    }

    @Override
    public Config fromDocument(Document document, NitriteMapper nitriteMapper) {
        Config config = new Config();
        config.setPort(CustomConverterReadsHelper.readInt(document, "port", config.getPort()));
        config.setMaxUsers(CustomConverterReadsHelper.readInt(document, "maxUsers", config.getMaxUsers()));

        return config;
    }


}
