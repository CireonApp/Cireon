package com.cireonapp.server.util;

import java.time.LocalDateTime;

public class TimeHelper {
    public static String getCurrentTimeISO() {
        return LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_DATE_TIME);
    }
}
