package com.github.arrivedbog593.borderlesswindow.general;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;

/**
 * HUD toggles. For now only one: the name of the held item that vanilla
 * shows above the hotbar for a couple of seconds after switching slots
 * (OptiFine's "Held Item Tooltips"). Each loader draws it its own way:
 * GuiMixin hides it on Fabric, and BorderlessWindowNeoForge cancels
 * NeoForge's SELECTED_ITEM_NAME HUD layer.
 * <p>
 * true = shown (vanilla, the default). The setter persists immediately;
 * init() is the only entry that does not save.
 */
public final class HudState {

    private static boolean heldItemName = true;

    private HudState() {
    }

    /** Called once at startup with the value loaded from disk. */
    public static void init(boolean loadedHeldItemName) {
        heldItemName = loadedHeldItemName;
    }

    public static boolean isHeldItemNameEnabled() {
        return heldItemName;
    }

    public static void setHeldItemNameEnabled(boolean enabled) {
        heldItemName = enabled;
        BorderlessConfigFile.saveCurrent();
    }
}
