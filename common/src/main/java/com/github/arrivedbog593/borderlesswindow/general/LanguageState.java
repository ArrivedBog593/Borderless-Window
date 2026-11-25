package com.github.arrivedbog593.borderlesswindow.general;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;

/**
 * Fast language switching. Vanilla reloads every resource (textures,
 * models, sounds, shaders) behind the loading screen when the language
 * changes; with this on, LanguageSelectScreenMixin reloads only the
 * translations, which applies the new language almost at once.
 * <p>
 * The language reload is complete for Minecraft itself: LanguageManager's
 * reload callback rebuilds the creative inventory and recipe book search
 * trees in the new language. JEI, EMI and REI are brought up to date too
 * (RecipeViewerSearchRefresh). Other mods that cache translated names and
 * only refresh them on a full resource reload may keep the old language
 * until the next one; F3+T forces it.
 * <p>
 * true = fast (the default). The setter persists immediately; init() is
 * the only entry that does not save.
 */
public final class LanguageState {

    private static boolean fastReload = true;

    private LanguageState() {
    }

    /** Called once at startup with the value loaded from disk. */
    public static void init(boolean loadedFastReload) {
        fastReload = loadedFastReload;
    }

    public static boolean isFastReloadEnabled() {
        return fastReload;
    }

    public static void setFastReloadEnabled(boolean enabled) {
        fastReload = enabled;
        BorderlessConfigFile.saveCurrent();
    }
}
