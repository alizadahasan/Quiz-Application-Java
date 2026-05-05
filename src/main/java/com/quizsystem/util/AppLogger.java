package com.quizsystem.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Shared application logger for consistent non-UI error reporting.
 */
public final class AppLogger {
    private static final Logger LOGGER = Logger.getLogger("com.quizsystem");

    private AppLogger() {
    }

    public static void warn(String message) {
        LOGGER.log(Level.WARNING, message);
    }

    public static void error(String message) {
        LOGGER.log(Level.SEVERE, message);
    }

    public static void error(String message, Throwable throwable) {
        LOGGER.log(Level.SEVERE, message, throwable);
    }
}
