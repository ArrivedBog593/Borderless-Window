package com.github.arrivedbog593.borderlesswindow.config;

import com.github.arrivedbog593.borderlesswindow.BorderlessHandler;
import com.github.arrivedbog593.borderlesswindow.F11Mode;
import com.github.arrivedbog593.borderlesswindow.ScreenMode;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;

/**
 * The Sodium Config API is only a presentation layer: it does not store
 * anything by itself. This class is the minimal "storage" it asks for:
 * when the user changes an option in the menu, the actual change is
 * applied to the window immediately through BorderlessHandler.
 * <p>
 * The real persistence lives in BorderlessConfigFile
 * (config/borderlesswindow.json), which BorderlessHandler saves
 * automatically on every mode change. Additionally, the WindowMixin
 * override of isFullscreen() keeps the vanilla "fullscreen yes/no" flag
 * in options.txt consistent with the current mode.
 */
public final class ScreenModeStorage {

    private ScreenMode pendingValue = BorderlessHandler.getCurrentMode();
    private boolean screenModeDirty = false;

    private F11Mode pendingF11Target = BorderlessHandler.getF11Target();
    private boolean f11TargetDirty = false;

    public ScreenMode getScreenMode() {
        return BorderlessHandler.getCurrentMode();
    }

    public void setScreenMode(ScreenMode mode) {
        this.pendingValue = mode;
        this.screenModeDirty = true;
    }

    public F11Mode getF11Target() {
        return BorderlessHandler.getF11Target();
    }

    public void setF11Target(F11Mode target) {
        this.pendingF11Target = target;
        this.f11TargetDirty = true;
    }

    /**
     * Called when Sodium applies pending changes (setStorageHandler).
     * <p>
     * IMPORTANT: we only apply what the user ACTUALLY changed in the menu
     * (dirty flags). Previously, flush() always applied pendingValue --
     * and since pendingValue is only updated when the option is touched
     * in the menu, applying any other option after using F11 reverted the
     * mode back to the stale cached value.
     */
    public void flush() {
        if (this.f11TargetDirty) {
            BorderlessHandler.setF11Target(this.pendingF11Target);
            this.f11TargetDirty = false;
        }

        if (this.screenModeDirty) {
            Window window = Minecraft.getInstance().getWindow();
            BorderlessHandler.setMode(window, this.pendingValue);
            this.screenModeDirty = false;
        }
    }
}