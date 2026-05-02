package com.quizsystem.util;

import javafx.scene.Scene;

import java.net.URL;

/**
 * Centralized theme handling for all JavaFX scenes.
 */
public final class ThemeManager {
    private static final String LIGHT_CSS = "/com/quizsystem/ui/styles.css";
    private static final String DARK_CSS = "/com/quizsystem/ui/dark-mode.css";

    private static boolean darkModeEnabled;

    private ThemeManager() {
    }

    public static void apply(Scene scene) {
        if (scene == null) {
            return;
        }

        scene.getStylesheets().clear();
        String stylesheet = darkModeEnabled ? DARK_CSS : LIGHT_CSS;
        URL stylesheetUrl = ThemeManager.class.getResource(stylesheet);
        if (stylesheetUrl == null) {
            System.err.println("Warning: " + stylesheet + " not found.");
            return;
        }

        scene.getStylesheets().add(stylesheetUrl.toExternalForm());
    }

    public static void toggle(Scene scene) {
        darkModeEnabled = !darkModeEnabled;
        apply(scene);
    }

    public static boolean isDarkModeEnabled() {
        return darkModeEnabled;
    }
}
