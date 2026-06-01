package com.cireonapp.server.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class TimeHelper {
    public static String getCurrentTimeISO() {
        return Instant.now().toString();
    }

    public static String getReadableTimeFromISO(String iso) {
        if (iso == null || iso.isBlank()) {
            return "N/A";
        }

        LocalDateTime dateTime;
        try {
            dateTime = LocalDateTime.ofInstant(Instant.parse(iso), ZoneId.systemDefault());
        } catch (DateTimeParseException ignored) {
            DateTimeFormatter parser = new DateTimeFormatterBuilder()
                    .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    .toFormatter();
            dateTime = LocalDateTime.parse(iso, parser);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy, h:mm:ss a", Locale.US);

        return dateTime.format(formatter);
    }

    public static long parseTimeFromISO(String iso) {
        if (iso == null || iso.isBlank()) {
            return -1;
        }
        try {
            return Instant.parse(iso).getEpochSecond();
        } catch (DateTimeParseException ignored) {
            try {
                return LocalDateTime.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        .atZone(ZoneId.systemDefault())
                        .toEpochSecond();
            } catch (DateTimeParseException ignoredToo) {
                return -1;
            }
        }
    }

    public static long getCurrentTime() {
        return Instant.now().atOffset(ZoneOffset.UTC).toEpochSecond();
    }
}
