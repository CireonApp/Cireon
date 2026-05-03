package com.cireonapp.server.util;

import org.dizitart.no2.collection.Document;

import java.nio.file.Path;

public class CustomConverterReadsHelper {
    public static Path readPath(Document document, String key) {
        String pathStr = document.get(key, String.class);
        if (pathStr == null) {
            return null;
        }
        return Path.of(pathStr);
    }

    public static int readInt(Document document, String key, int defaultValue) {
        Object value = document.get(key);
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Number number) {
            return number.intValue();
        }

        if (value instanceof String strValue) {
            try {
                return Integer.parseInt(strValue);
            } catch (NumberFormatException ignored) {
                return defaultValue;
            }
        }

        return defaultValue;
    }
}
