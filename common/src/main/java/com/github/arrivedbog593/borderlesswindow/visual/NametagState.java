package com.github.arrivedbog593.borderlesswindow.visual;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;

/**
 * Which nametags are drawn (NametagMode), read by EntityRendererMixin.
 * Drawing only: names stay on the entities, in chat, on the tab list and
 * in death messages.
 * <p>
 * The setter persists immediately; init() is the only entry that does
 * not save.
 */
public final class NametagState {

    private static NametagMode mode = NametagMode.ALL;

    private NametagState() {
    }

    /** Called once at startup with the value loaded from disk. */
    public static void init(NametagMode loadedMode) {
        mode = loadedMode;
    }

    public static NametagMode getMode() {
        return mode;
    }

    public static void setMode(NametagMode newMode) {
        mode = newMode;
        BorderlessConfigFile.saveCurrent();
    }
}
