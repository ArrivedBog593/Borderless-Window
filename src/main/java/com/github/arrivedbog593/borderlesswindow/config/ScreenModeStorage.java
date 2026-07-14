package com.github.arrivedbog593.borderlesswindow.config;

import com.github.arrivedbog593.borderlesswindow.BorderlessHandler;
import com.github.arrivedbog593.borderlesswindow.F11Mode;
import com.github.arrivedbog593.borderlesswindow.FpsOverlayMode;
import com.github.arrivedbog593.borderlesswindow.FpsOverlayPosition;
import com.github.arrivedbog593.borderlesswindow.FpsOverlayState;
import com.github.arrivedbog593.borderlesswindow.ScreenMode;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;

/**
 * The Sodium Config API is only a presentation layer: it does not store
 * anything by itself. This class is the minimal "storage" it asks for:
 * when the user changes an option in the menu, the actual change is
 * applied immediately through the corresponding state holder
 * (BorderlessHandler for window state, FpsOverlayState for the overlay).
 * <p>
 * The real persistence lives in BorderlessConfigFile
 * (config/borderlesswindow.json), which the state holders save
 * automatically on every change. Additionally, the WindowMixin
 * override of isFullscreen() keeps the vanilla "fullscreen yes/no" flag
 * in options.txt consistent with the current mode.
 */
public final class ScreenModeStorage {

    private ScreenMode pendingValue = BorderlessHandler.getCurrentMode();
    private boolean screenModeDirty = false;

    private F11Mode pendingF11Target = BorderlessHandler.getF11Target();
    private boolean f11TargetDirty = false;

    private FpsOverlayMode pendingFpsOverlayMode = FpsOverlayState.getMode();
    private boolean fpsOverlayModeDirty = false;

    private FpsOverlayPosition pendingFpsOverlayPosition = FpsOverlayState.getPosition();
    private boolean fpsOverlayPositionDirty = false;

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

    public FpsOverlayMode getFpsOverlayMode() {
        return FpsOverlayState.getMode();
    }

    public void setFpsOverlayMode(FpsOverlayMode mode) {
        this.pendingFpsOverlayMode = mode;
        this.fpsOverlayModeDirty = true;
    }

    public FpsOverlayPosition getFpsOverlayPosition() {
        return FpsOverlayState.getPosition();
    }

    public void setFpsOverlayPosition(FpsOverlayPosition position) {
        this.pendingFpsOverlayPosition = position;
        this.fpsOverlayPositionDirty = true;
    }

    /**
     * Called when Sodium applies pending changes (setStorageHandler).
     * <p>
     * IMPORTANT: we only apply what the user ACTUALLY changed in the menu
     * (dirty flags). Previously, flush() always applied pendingValue --
     * and since pendingValue is only updated when the option is touched
     * in the menu, applying any other option after using F11 reverted the
     * mode back to the stale cached value. The same rule protects every
     * option added since.
     */
    public void flush() {
        if (this.f11TargetDirty) {
            BorderlessHandler.setF11Target(this.pendingF11Target);
            this.f11TargetDirty = false;
        }

        if (this.fpsOverlayModeDirty) {
            FpsOverlayState.setMode(this.pendingFpsOverlayMode);
            this.fpsOverlayModeDirty = false;
        }

        if (this.fpsOverlayPositionDirty) {
            FpsOverlayState.setPosition(this.pendingFpsOverlayPosition);
            this.fpsOverlayPositionDirty = false;
        }

        if (this.screenModeDirty) {
            Window window = Minecraft.getInstance().getWindow();
            BorderlessHandler.setMode(window, this.pendingValue);
            this.screenModeDirty = false;
        }
    }
}
