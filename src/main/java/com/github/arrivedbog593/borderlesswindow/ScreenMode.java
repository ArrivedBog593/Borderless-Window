package com.github.arrivedbog593.borderlesswindow;

/**
 * Los 3 estados que puede tener la ventana. Este enum se usa tanto
 * internamente (BorderlessHandler) como en la opcion que se muestra
 * en el menu de Sodium (via la Config API).
 */
public enum ScreenMode {
    WINDOWED("Sin pantalla completa"),
    BORDERLESS("Sin bordes"),
    FULLSCREEN("Pantalla completa");

    private final String displayName;

    ScreenMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
