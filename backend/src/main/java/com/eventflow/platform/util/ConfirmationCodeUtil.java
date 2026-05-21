package com.eventflow.platform.util;

import java.util.UUID;

public final class ConfirmationCodeUtil {

    private ConfirmationCodeUtil() {
    }

    /** Generates a short, human-readable confirmation code: EF + 10 uppercase alphanumeric chars. */
    public static String generate() {
        String raw = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return "EF" + raw.substring(0, 10);
    }
}
